#!/usr/bin/env python3
"""Snapshot www.thebridgeto.ai as a static site.

Fetches every page reachable from the sitemap and from internal links, downloads every
asset those pages reference from cdn.thebridgeto.ai (plus the root files browsers
request on their own), rewrites both hostnames to the target hostname, and writes the
result under dist/ ready for deploy.sh.

    python3 build.py                                   # dist/ for https://v2.thebridgeto.ai
    python3 build.py --target https://www.thebridgeto.ai   # the same tree for a later cutover
    python3 build.py --limit 20                        # quick smoke test

Storage rules (the CloudFront viewer-request function in infra/website.yaml applies the
same mapping to incoming requests):

    /                                 -> index.html
    /about/   and   /about            -> about/index.html
    /section/<uuid>/<any-slug>        -> section/<uuid>/index.html   (the Java site ignores the slug)
    /our-thinking.action?cursor=...   -> our-thinking/page/2/index.html
    https://cdn.thebridgeto.ai/<path> -> <path>   (same key, same content type as the CDN)

Only the Python standard library is used.
"""
import argparse
import hashlib
import html
import json
import mimetypes
import os
import re
import shutil
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone

SOURCE_SITE = "https://www.thebridgeto.ai"
SOURCE_CDN = "https://cdn.thebridgeto.ai"
DEFAULT_TARGET = "https://v2.thebridgeto.ai"
USER_AGENT = "Mozilla/5.0 (compatible; TheBridgeToAI-static-snapshot/1.0)"

# Root files that browsers (and site.webmanifest) request without any page linking to them.
ROOT_FILES = [
    "/sitemap.xml", "/robots.txt", "/favicon.ico", "/favicon-16x16.png", "/favicon-32x32.png",
    "/apple-touch-icon.png", "/android-chrome-192x192.png", "/android-chrome-512x512.png",
    "/site.webmanifest",
]

# Application endpoints on www that are not content pages (see robots.txt on the live site).
SKIP_PREFIXES = ("/rest/", "/mcp/", "/oauth2/", "/admin", "/signin", "/callback", "/error", "/login")

FILE_EXT_RE = re.compile(
    r"\.(?:html?|css|js|mjs|map|json|xml|txt|ico|png|jpe?g|gif|svg|webp|avif|mp3|mp4|m4a|wav|ogg"
    r"|webm|pdf|woff2?|ttf|otf|eot|webmanifest|zip|csv|md|ics|vcf)$", re.I)
UUID = r"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
CONTENT_PAGE_RE = re.compile(r"^(/[^/]+(?:/[^/]+)*?/" + UUID + r")(?:/.*)?$")
URL_RE = re.compile(r"https://(?:www|cdn)\.thebridgeto\.ai(?:/[^\s\"'<>`)]*)?")
CSS_REF_RE = re.compile(r"url\(\s*['\"]?([^'\")]+?)['\"]?\s*\)|@import\s+['\"]([^'\"]+)['\"]")
LOC_RE = re.compile(r"<loc>\s*([^<\s]+)\s*</loc>")

CONTENT_TYPE_BY_EXT = {
    ".html": "text/html; charset=utf-8", ".webmanifest": "application/manifest+json",
    ".svg": "image/svg+xml", ".ico": "image/x-icon", ".xml": "application/xml",
    ".txt": "text/plain; charset=utf-8", ".css": "text/css", ".js": "application/javascript",
    ".json": "application/json", ".mp3": "audio/mpeg", ".woff2": "font/woff2", ".woff": "font/woff",
}
TEXT_TYPES = ("text/", "application/json", "application/xml", "application/javascript",
              "application/x-javascript", "application/manifest+json", "image/svg+xml")

NOT_FOUND_HTML = (
    '<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">\n'
    "<html><head>\n<title>404 Not Found</title>\n</head><body>\n<h1>Not Found</h1>\n"
    "<p>The requested URL was not found on this server.</p>\n</body></html>\n"
)


def log(msg):
    print(msg, flush=True)


def safe_url(url):
    """Percent-encode anything urllib cannot put on the wire; leave the rest exactly as linked."""
    return urllib.parse.quote(url, safe=":/?#[]@!$&'()*+,;=%~-._")


def fetch(url, attempts=3):
    """Return (status, content_type, body). Follows redirects. Retries on network errors only."""
    last = None
    for attempt in range(attempts):
        try:
            req = urllib.request.Request(safe_url(url), headers={"User-Agent": USER_AGENT, "Accept": "*/*"})
            with urllib.request.urlopen(req, timeout=90) as resp:
                return resp.status, resp.headers.get("Content-Type", "") or "", resp.read()
        except urllib.error.HTTPError as err:
            return err.code, err.headers.get("Content-Type", "") or "", err.read()
        except Exception as err:  # noqa: BLE001 - any transport error is retried
            last = err
            time.sleep(1 + attempt)
    raise RuntimeError(f"{url}: {last}")


def classify(url):
    """Map a URL found in the source to (kind, key, fetch_url), or None to ignore it.

    kind is 'page' (key = canonical directory path ending in /), 'asset' (key = object path),
    'cursor' (key = the raw paginated listing URL) or 'skip'.
    """
    url = html.unescape(url.strip())
    if any(ch in url for ch in "${}`\\"):        # fragments of JavaScript template strings
        return None
    parts = urllib.parse.urlsplit(url)
    host, path, query = parts.netloc, parts.path or "/", parts.query
    if host == "cdn.thebridgeto.ai":
        return ("asset", path, SOURCE_CDN + path) if path != "/" else None
    if host != "www.thebridgeto.ai":
        return None
    if path == "/our-thinking.action" and query.startswith("cursor="):
        return ("cursor", url, url)
    if FILE_EXT_RE.search(path):            # includes /rest/api/1/event/<uuid>.ics calendar downloads
        return ("asset", path, SOURCE_SITE + path)
    if path.endswith(".action") or path.startswith(SKIP_PREFIXES):
        return ("skip", path, url)
    match = CONTENT_PAGE_RE.match(path)
    if match:
        canonical = match.group(1) + "/"
    else:
        canonical = path if path.endswith("/") else path + "/"
    return ("page", canonical, url)


def content_type_for(key, header_value):
    ctype = (header_value or "").split(";")[0].strip().lower()
    ext = os.path.splitext(key)[1].lower()
    if ext == ".html":
        return CONTENT_TYPE_BY_EXT[".html"]
    if ctype and ctype not in ("application/octet-stream", "binary/octet-stream"):
        return header_value.strip()
    return CONTENT_TYPE_BY_EXT.get(ext) or mimetypes.guess_type(key)[0] or ctype or "application/octet-stream"


class Snapshot:
    def __init__(self, target, workers):
        self.target = target.rstrip("/")
        self.workers = workers
        self.pages = {}       # canonical path -> record
        self.assets = {}      # object path -> record
        self.cursors = {}     # raw cursor URL (unescaped) -> canonical page path
        self.skipped = set()
        self.pending_pages = []
        self.pending_assets = []

    # ------------------------------------------------------------------ discovery
    def add(self, url, found_on):
        info = classify(url)
        if info is None:
            return
        kind, key, fetch_url = info
        if kind == "skip":
            self.skipped.add(key)
        elif kind == "cursor":
            if key not in self.cursors:
                number = len(self.cursors) + 2
                canonical = f"/our-thinking/page/{number}/"
                self.cursors[key] = canonical
                self.pages[canonical] = {"source": fetch_url, "found_on": found_on}
                self.pending_pages.append((canonical, fetch_url))
        elif kind == "page":
            if key not in self.pages:
                self.pages[key] = {"source": fetch_url, "found_on": found_on}
                self.pending_pages.append((key, fetch_url))
        elif kind == "asset":
            if key not in self.assets:
                self.assets[key] = {"source": fetch_url, "found_on": found_on}
                self.pending_assets.append((key, fetch_url))

    def discover_in_html(self, text, found_on):
        for match in URL_RE.finditer(text):
            self.add(match.group(0), found_on)

    def discover_in_css(self, text, css_url, found_on):
        for match in CSS_REF_RE.finditer(text):
            ref = (match.group(1) or match.group(2) or "").strip()
            if not ref or ref.startswith("data:"):
                continue
            self.add(urllib.parse.urljoin(css_url, ref), found_on)

    # ------------------------------------------------------------------ fetching
    def fetch_pages(self, limit):
        while self.pending_pages:
            if limit and sum(1 for p in self.pages.values() if "status" in p) >= limit:
                self.pending_pages.clear()
                break
            batch, self.pending_pages = self.pending_pages, []
            with ThreadPoolExecutor(self.workers) as pool:
                results = list(pool.map(lambda item: (item[0], fetch(item[1])), batch))
            for key, (status, ctype, body) in results:
                rec = self.pages[key]
                rec["status"] = status
                if status != 200 or "html" not in ctype.lower():
                    rec["error"] = f"HTTP {status} {ctype}"
                    log(f"  page  {status} {rec['source']}")
                    continue
                rec["html"] = body.decode("utf-8", "replace")
                self.discover_in_html(rec["html"], key)
            log(f"pages fetched: {sum(1 for p in self.pages.values() if 'status' in p)}, "
                f"queued: {len(self.pending_pages)}, assets known: {len(self.assets)}")

    def fetch_assets(self):
        while self.pending_assets:
            batch, self.pending_assets = self.pending_assets, []
            with ThreadPoolExecutor(self.workers) as pool:
                results = list(pool.map(lambda item: (item[0], fetch(item[1])), batch))
            for key, (status, ctype, body) in results:
                rec = self.assets[key]
                rec["status"] = status
                if status != 200:
                    rec["error"] = f"HTTP {status}"
                    log(f"  asset {status} {rec['source']}")
                    continue
                rec["content_type"] = content_type_for(key, ctype)
                rec["data"] = body
                if rec["content_type"].startswith("text/css"):
                    self.discover_in_css(body.decode("utf-8", "replace"), rec["source"], key)
            log(f"assets fetched: {sum(1 for a in self.assets.values() if 'status' in a)}, "
                f"queued: {len(self.pending_assets)}")

    # ------------------------------------------------------------------ output
    def rewrite(self, text):
        for raw, canonical in self.cursors.items():
            for form in {raw, html.escape(raw, quote=False), html.escape(raw, quote=True)}:
                text = text.replace(form, self.target + canonical)
        return text.replace(SOURCE_SITE, self.target).replace(SOURCE_CDN, self.target)

    def write(self, out_dir, manifest_path):
        tmp_dir = out_dir + ".tmp"
        shutil.rmtree(tmp_dir, ignore_errors=True)
        os.makedirs(tmp_dir)
        files = []

        def emit(key, data, content_type, source):
            rel = key.lstrip("/")
            dest = os.path.join(tmp_dir, rel)
            os.makedirs(os.path.dirname(dest) or tmp_dir, exist_ok=True)
            with open(dest, "wb") as fh:
                fh.write(data)
            files.append({"key": rel, "content_type": content_type, "bytes": len(data),
                          "sha256": hashlib.sha256(data).hexdigest(), "source": source})

        for canonical, rec in sorted(self.pages.items()):
            if "html" not in rec:
                continue
            emit(canonical + "index.html", self.rewrite(rec["html"]).encode("utf-8"),
                 CONTENT_TYPE_BY_EXT[".html"], rec["source"])
        for key, rec in sorted(self.assets.items()):
            if "data" not in rec:
                continue
            data = rec["data"]
            if rec["content_type"].lower().startswith(TEXT_TYPES):
                data = self.rewrite(data.decode("utf-8", "replace")).encode("utf-8")
            emit(key, data, rec["content_type"], rec["source"])
        emit("/404.html", NOT_FOUND_HTML.encode("utf-8"), CONTENT_TYPE_BY_EXT[".html"], "generated")

        shutil.rmtree(out_dir, ignore_errors=True)
        os.rename(tmp_dir, out_dir)

        missing = sorted(
            [{"kind": "page", "source": r["source"], "error": r["error"], "found_on": r.get("found_on")}
             for r in self.pages.values() if "error" in r] +
            [{"kind": "asset", "source": r["source"], "error": r["error"], "found_on": r.get("found_on")}
             for r in self.assets.values() if "error" in r],
            key=lambda m: m["source"])
        manifest = {
            "built_at": datetime.now(timezone.utc).isoformat(timespec="seconds"),
            "source_site": SOURCE_SITE, "source_cdn": SOURCE_CDN, "target": self.target,
            "pages": sum(1 for r in self.pages.values() if "html" in r),
            "assets": sum(1 for r in self.assets.values() if "data" in r),
            "bytes": sum(f["bytes"] for f in files),
            "paginated_listings": {raw: canonical for raw, canonical in self.cursors.items()},
            "missing": missing, "skipped_endpoints": sorted(self.skipped), "files": files,
        }
        with open(manifest_path, "w") as fh:
            json.dump(manifest, fh, indent=1)
        return manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--target", default=DEFAULT_TARGET, help="hostname the copy is served from")
    parser.add_argument("--out", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "dist"))
    parser.add_argument("--manifest", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "build-manifest.json"))
    parser.add_argument("--workers", type=int, default=8)
    parser.add_argument("--limit", type=int, default=0, help="stop after roughly this many pages (smoke test)")
    args = parser.parse_args()

    started = time.time()
    snap = Snapshot(args.target, args.workers)
    log(f"snapshot of {SOURCE_SITE} for {snap.target}")

    status, _, body = fetch(SOURCE_SITE + "/sitemap.xml")
    if status != 200:
        sys.exit(f"could not fetch sitemap.xml: HTTP {status}")
    snap.add(SOURCE_SITE + "/", "seed")
    for loc in LOC_RE.findall(body.decode("utf-8", "replace")):
        snap.add(loc, "sitemap.xml")
    for path in ROOT_FILES:
        snap.add(SOURCE_SITE + path, "seed")

    snap.fetch_pages(args.limit)
    snap.fetch_assets()
    manifest = snap.write(args.out, args.manifest)

    leftovers = 0
    for root, _, names in os.walk(args.out):
        for name in names:
            if name.endswith((".html", ".css", ".js", ".xml", ".txt", ".webmanifest", ".json")):
                with open(os.path.join(root, name), "rb") as fh:
                    text = fh.read()
                leftovers += text.count(SOURCE_SITE.encode()) + text.count(SOURCE_CDN.encode())

    log("")
    log(f"pages written:   {manifest['pages']}")
    log(f"assets written:  {manifest['assets']}")
    log(f"total size:      {manifest['bytes'] / 1e6:.1f} MB")
    log(f"paginated:       {manifest['paginated_listings']}")
    log(f"missing (not copied, they fail on the live site too): {len(manifest['missing'])}")
    for item in manifest["missing"]:
        log(f"  {item['error']:<12} {item['source']}  (found on {item['found_on']})")
    log(f"skipped app endpoints: {manifest['skipped_endpoints']}")
    log(f"unrewritten source hostnames left in text files: {leftovers}")
    log(f"output: {args.out}  manifest: {args.manifest}  ({time.time() - started:.0f}s)")


if __name__ == "__main__":
    main()
