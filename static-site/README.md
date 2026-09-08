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
| `/events/<uuid>.ics` | the event calendar downloads, moved out of `/rest/` (they were `/rest/api/1/event/<uuid>.ics` on www) |
| `/rest/<path>` | not static: proxied to `https://n8n.thebridgeto.ai/webhook/<path>` (see below) |

Anything else returns the same bare "404 Not Found" page Apache serves on www.

## Forms and the /rest/ proxy

Every `/rest/*` request on v2 is passed straight through to n8n with the `/rest` prefix swapped
for `/webhook`: nothing is cached, all methods are allowed, and headers, query string and body
are forwarded as sent. Registering a new webhook under `/webhook/` in n8n makes it available
under `/rest/` on the site with no stack change. The origin host and base path are the
`ApiOriginDomain` and `ApiOriginBasePath` stack parameters.

The snapshot repoints the four forms at the two webhooks that exist today:

| Form | Posts to | JSON body |
|------|----------|-----------|
| /contact/ | `/rest/btai/contact/` | `name, email, subject, message` |
| /newsletter/ and /bridge-network/operator/ | `/rest/btai/subscribe/` | `email, name` |
| /bridge-network/builder/ | `/rest/btai/subscribe/` | `email, firstName` |

The pages treat any 2xx as success; a subscribe response of `{"status":"already_subscribed"}`
switches the thank-you text, and a 4xx/5xx with `{"error":"..."}` shows that message.

Two caveats. CloudFront's custom error page is distribution-wide; only 403 is mapped to it (S3
signals a missing object with 403 because the bucket policy grants no ListBucket), so n8n's own
404 responses such as "webhook not registered" pass through, while a 403 from n8n or the WAF
would show as the static 404 page. And the people pages still call `/rest/api/1/content/related`
and the events page `/rest/api/1/event/{upcoming,past}`, which now reach n8n as
`/webhook/api/1/...` and 404 until workflows exist there (the same fallback text as today).

A webhook answers "not registered" until its workflow is switched to Active in n8n with the
Webhook node's method set to POST; test-mode listening only serves the `/webhook-test/` URL,
which the proxy does not cover. Note that n8n reports that condition as a 404 to curl but as a
bare 500 to a browser (any request carrying an Origin header), directly or through the proxy.
`verify.py` sends a CORS preflight to each form webhook and fails unless POST is registered.

## What is deliberately not copied

- `GET /rest/api/1/content/related` (related profiles on people pages) returns HTTP 400 on the
  live site, so those pages already show their fallback message; v2 behaves the same.
- The per-page JS bundles under `/49/dist/js/` that the pages reference do not exist on the CDN
  (they fail there too), and `/models/` in the sitemap is a 404 on www as well.

## Cutting over to www later

`build.py --target https://www.thebridgeto.ai` produces the same tree with www links, and the
stack takes `SITE_HOSTNAME=www.thebridgeto.ai` (a separate stack name and the www DNS record
would need to move from the ALB to CloudFront). The `/rest/*` proxy carries over unchanged.
