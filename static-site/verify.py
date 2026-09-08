#!/usr/bin/env python3
"""Check the deployed copy against build-manifest.json.

For every file the build wrote, fetches it from the target host and compares the bytes.
Pages are also requested in the other forms the live site accepts (no trailing slash,
UUID with a different slug), unknown paths must return the 404 page, plain HTTP must
redirect to HTTPS, and /rest/* must reach the API origin: a CORS preflight to each form
webhook must come back from n8n with POST among the allowed methods, which is only the case
while that workflow is active (nothing is posted).

    python3 verify.py                         # against https://v2.thebridgeto.ai
    python3 verify.py --target https://d123.cloudfront.net
"""
import argparse
import hashlib
import http.client
import json
import os
import re
import sys
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor

UUID_DIR_RE = re.compile(r"^(.*/)[0-9a-fA-F-]{36}/index\.html$")


def get(url, method="GET", extra_headers=None):
    headers = {"User-Agent": "TheBridgeToAI-static-verify/1.0"}
    headers.update(extra_headers or {})
    req = urllib.request.Request(url, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            return resp.status, resp.headers, resp.read()
    except urllib.error.HTTPError as err:
        return err.code, err.headers, err.read()


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--target", default=None, help="defaults to the target recorded in the manifest")
    parser.add_argument("--manifest", default=os.path.join(here, "build-manifest.json"))
    parser.add_argument("--dist", default=os.path.join(here, "dist"))
    parser.add_argument("--workers", type=int, default=8)
    args = parser.parse_args()

    manifest = json.load(open(args.manifest))
    target = (args.target or manifest["target"]).rstrip("/")
    host = target.split("//", 1)[1]

    checks = []   # (label, url, expected_sha256 or None, expected_status)
    for entry in manifest["files"]:
        key = entry["key"]
        if key == "404.html":
            continue
        if key.endswith("/index.html"):
            directory = "/" + key[: -len("index.html")]
            checks.append(("page", target + directory, entry["sha256"], 200))
            checks.append(("page-noslash", target + directory.rstrip("/"), entry["sha256"], 200))
            if UUID_DIR_RE.match(key):
                checks.append(("page-anyslug", target + directory + "some-other-slug", entry["sha256"], 200))
        elif key == "index.html":
            checks.append(("page", target + "/", entry["sha256"], 200))
        else:
            checks.append(("asset", target + "/" + key, entry["sha256"], 200))
    not_found = hashlib.sha256(open(os.path.join(args.dist, "404.html"), "rb").read()).hexdigest()
    checks.append(("404", target + "/models/", not_found, 404))
    checks.append(("404", target + "/definitely/not/here", not_found, 404))
    for path in ("/rest/btai/subscribe/", "/rest/btai/contact/"):
        checks.append(("rest-proxy", target + path, None, 204))

    def run(check):
        label, url, digest, expected = check
        try:
            if label == "rest-proxy":
                status, headers, body = get(url, "OPTIONS", {
                    "Origin": target, "Access-Control-Request-Method": "POST",
                    "Access-Control-Request-Headers": "content-type"})
            else:
                status, headers, body = get(url)
        except Exception as err:  # noqa: BLE001
            return (label, url, f"error {err}")
        if status != expected:
            return (label, url, f"HTTP {status}, expected {expected}"
                    + (" (n8n answers 500 to a browser when the workflow is not active)" if label == "rest-proxy" else ""))
        if label == "rest-proxy":
            allowed = headers.get("Access-Control-Allow-Methods") or ""
            if "POST" not in allowed.upper():
                return (label, url, f"POST not registered (Access-Control-Allow-Methods: {allowed or 'absent'})")
        if digest and hashlib.sha256(body).hexdigest() != digest:
            return (label, url, f"content differs ({len(body)} bytes served)")
        if label.startswith("page") and not (headers.get("Content-Type") or "").startswith("text/html"):
            return (label, url, f"content type {headers.get('Content-Type')}")
        return None

    with ThreadPoolExecutor(args.workers) as pool:
        failures = [f for f in pool.map(run, checks) if f]

    # Plain HTTP must redirect to HTTPS.
    conn = http.client.HTTPConnection(host, 80, timeout=30)
    conn.request("GET", "/about/", headers={"Host": host})
    resp = conn.getresponse()
    location = resp.getheader("Location") or ""
    if resp.status not in (301, 302, 308) or not location.startswith("https://"):
        failures.append(("http-redirect", f"http://{host}/about/", f"HTTP {resp.status} Location {location}"))

    counts = {}
    for label, *_ in checks:
        counts[label] = counts.get(label, 0) + 1
    print(f"target: {target}")
    print("checks: " + ", ".join(f"{k}={v}" for k, v in sorted(counts.items())) + ", http-redirect=1")
    if failures:
        print(f"FAILED: {len(failures)}")
        for label, url, why in failures:
            print(f"  {label:<13} {url}  -> {why}")
        sys.exit(1)
    print(f"all {len(checks) + 1} checks passed")


if __name__ == "__main__":
    main()
