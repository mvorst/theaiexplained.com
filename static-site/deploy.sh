#!/usr/bin/env bash
# Uploads static-site/dist to the v2 site bucket and invalidates the CloudFront cache.
#
#   static-site/deploy.sh            upload dist/ (run build.py first)
#   static-site/deploy.sh --build    run build.py first
#   static-site/deploy.sh --dry-run  show what would change
#
# Environment overrides: AWS_PROFILE, STACK_NAME, SITE_DIR, MANIFEST.
set -euo pipefail
cd "$(dirname "$0")"

PROFILE="${AWS_PROFILE:-claude_prod_thebridgeto_ai}"
REGION=us-east-1
STACK="${STACK_NAME:-thebridgetoai-v2-website}"
SITE_DIR="${SITE_DIR:-$PWD/dist}"
MANIFEST="${MANIFEST:-$PWD/build-manifest.json}"
DRYRUN=0
BUILD=0
for arg in "$@"; do
  case "$arg" in
    --dry-run) DRYRUN=1 ;;
    --build) BUILD=1 ;;
    *) echo "unknown argument: $arg" >&2; exit 2 ;;
  esac
done

if [[ $BUILD -eq 1 ]]; then
  python3 ./build.py
fi
[[ -f "$SITE_DIR/index.html" && -f "$MANIFEST" ]] || { echo "no dist/index.html or manifest (run build.py first)" >&2; exit 1; }

output() {
  aws cloudformation describe-stacks --profile "$PROFILE" --region "$REGION" --stack-name "$STACK" \
    --query "Stacks[0].Outputs[?OutputKey=='$1'].OutputValue" --output text
}
BUCKET="$(output SiteBucketName)"
DIST_ID="$(output DistributionId)"
[[ -n "$BUCKET" && -n "$DIST_ID" ]] || { echo "stack $STACK has no outputs yet (run infra/deploy-infra.sh)" >&2; exit 1; }

common=(--profile "$PROFILE" --region "$REGION" --exclude ".DS_Store" --exclude "*/.DS_Store")
if [[ $DRYRUN -eq 1 ]]; then common+=(--dryrun); fi
short="public, max-age=300"        # HTML, sitemap, robots, manifest: a redeploy shows up within minutes
day="public, max-age=86400"        # build-numbered and UUID-named assets: a day in browser caches
text=(--include "*.html" --include "*.xml" --include "*.txt" --include "*.webmanifest")

# Files whose content type the CLI cannot guess from the name (UUID-named images copied from the
# CDN, site.webmanifest). build-manifest.json records the type each one was served with.
special_excludes=()
while IFS=$'\t' read -r ctype keys; do
  for key in $keys; do special_excludes+=(--exclude "$key"); done
done < <(python3 - "$MANIFEST" <<'PY'
import json, os, sys
groups = {}
for f in json.load(open(sys.argv[1]))["files"]:
    name = os.path.basename(f["key"])
    if "." not in name or name.endswith(".webmanifest"):
        groups.setdefault(f["content_type"], []).append(f["key"])
for ctype, keys in sorted(groups.items()):
    print(ctype + "\t" + " ".join(keys))
PY
)

# 1. Assets first, without deleting, so no page ever references a missing file.
aws s3 sync "$SITE_DIR" "s3://$BUCKET" "${common[@]}" --exclude "*.html" --exclude "*.xml" \
  --exclude "*.txt" --exclude "*.webmanifest" "${special_excludes[@]}" --cache-control "$day"

# 2. Assets that need an explicit content type, one sync per type.
python3 - "$MANIFEST" <<'PY' | while IFS=$'\t' read -r ctype keys; do
import json, os, sys
groups = {}
for f in json.load(open(sys.argv[1]))["files"]:
    name = os.path.basename(f["key"])
    if "." not in name or name.endswith(".webmanifest"):
        groups.setdefault(f["content_type"], []).append(f["key"])
for ctype, keys in sorted(groups.items()):
    print(ctype + "\t" + " ".join(keys))
PY
  includes=()
  for key in $keys; do includes+=(--include "$key"); done
  cache="$day"; [[ "$ctype" == application/manifest+json ]] && cache="$short"
  aws s3 sync "$SITE_DIR" "s3://$BUCKET" "${common[@]}" --exclude "*" "${includes[@]}" \
    --content-type "$ctype" --cache-control "$cache"
done

# 3. HTML pages (explicit UTF-8 content type), then sitemap/robots; stale pages are deleted.
aws s3 sync "$SITE_DIR" "s3://$BUCKET" "${common[@]}" --exclude "*" --include "*.html" \
  --delete --content-type "text/html; charset=utf-8" --cache-control "$short"
aws s3 sync "$SITE_DIR" "s3://$BUCKET" "${common[@]}" --exclude "*" --include "*.xml" --include "*.txt" \
  --delete --cache-control "$short"

# 4. Now that the new pages are live, drop assets nothing references any more.
aws s3 sync "$SITE_DIR" "s3://$BUCKET" "${common[@]}" --exclude "*.html" --exclude "*.xml" \
  --exclude "*.txt" --exclude "*.webmanifest" "${special_excludes[@]}" --delete --cache-control "$day"

if [[ $DRYRUN -eq 0 ]]; then
  aws cloudfront create-invalidation --profile "$PROFILE" --distribution-id "$DIST_ID" \
    --paths "/*" --query 'Invalidation.{Id:Id,Status:Status}' --output table
  echo "Deployed $SITE_DIR to s3://$BUCKET and invalidated distribution $DIST_ID"
fi
