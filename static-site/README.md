# Static copy of www.thebridgeto.ai at v2.thebridgeto.ai

A snapshot of the live Java site, served from S3 + CloudFront. Nothing here touches the
Java application; it only reads the public site and the CDN.

| Step | Command | What it does |
|------|---------|--------------|
| Infrastructure (once, or after editing `infra/website.yaml`) | `static-site/infra/deploy-infra.sh` | CloudFormation stack `thebridgetoai-v2-website` in us-east-1: bucket `us-east-1.v2.thebridgeto.ai`, CloudFront with origin access control, the clean-URL viewer function, security headers, access logs, an ACM certificate validated in the thebridgeto.ai Route 53 zone, and A/AAAA records for v2. |
| Snapshot | `python3 static-site/build.py` | Crawls the sitemap plus every internal link, copies every CDN asset the pages reference, rewrites both hostnames to v2, writes `dist/` and `build-manifest.json`. About a minute. |
| Upload | `static-site/deploy.sh` (or `--build` to snapshot first, `--dry-run` to preview) | `aws s3 sync` in stages so pages never reference a missing file, sets content types for the extensionless CDN images, invalidates the distribution. |
| Verify | `python3 static-site/verify.py` | Fetches every file from v2 and compares bytes, checks the alternate URL forms, the 404 page, and the HTTP to HTTPS redirect. |

All commands use the `claude_prod_thebridgeto_ai` AWS profile unless `AWS_PROFILE` is set.
`dist/` and `build-manifest.json` are build output and are ignored by git.

## How the copy is laid out

The live site answers `/about` and `/about/` with the same page and ignores the slug after a
content UUID (`/our-thinking/<uuid>/<anything>`). The snapshot keeps one `index.html` per page
and the CloudFront function maps every request form onto it:

| Request | Object |
|---------|--------|
| `/` | `index.html` |
| `/about`, `/about/` | `about/index.html` |
| `/resources/people/<uuid>/<any-slug>` | `resources/people/<uuid>/index.html` |
| `/our-thinking.action?cursor=...` (older posts) | `our-thinking/page/2/index.html`, and the link on the listing page is rewritten to that URL |
| `/<uuid>` (extensionless image copied from the CDN) | served as-is with its original content type |
| `/49/...`, `/ai-image/...`, `/ai-audio/...`, `/external/...` | copied from cdn.thebridgeto.ai under the same paths |

Anything else returns the same bare "404 Not Found" page Apache serves on www.

## What is deliberately not copied

- `POST /rest/api/1/contact` and `POST /rest/api/1/subscribe` (the contact and newsletter forms)
  have no backend on v2 yet; the forms render but submitting shows the page's own error text.
- `GET /rest/api/1/content/related` (related profiles on people pages) returns HTTP 400 on the
  live site, so those pages already show their fallback message; v2 behaves the same.
- The per-page JS bundles under `/49/dist/js/` that the pages reference do not exist on the CDN
  (they fail there too), and `/models/` in the sitemap is a 404 on www as well.

## Cutting over to www later

`build.py --target https://www.thebridgeto.ai` produces the same tree with www links, and the
stack takes `SITE_HOSTNAME=www.thebridgeto.ai` (a separate stack name and the www DNS record
would need to move from the ALB to CloudFront). Form posts need a `/rest/*` behavior that
points at whatever replaces the Java endpoints.
