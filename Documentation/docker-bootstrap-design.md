# Docker Website Bootstrap — Design Document

**Version:** 0.6 (Draft)
**Date:** April 4, 2026

---

## 1. Overview

> **Scope:** This design covers the **development build environment**. Production deployments use dedicated EC2 instances behind load balancers and are out of scope for this document.

This document describes a Docker-based deployment process for hosting multiple client web applications on a single EC2 instance. Each client gets an isolated Docker container running Apache HTTPD and a Spring Boot application, managed by **supervisord** for reliable process supervision. At container startup, a versioned archive is downloaded from S3 using AWS credentials, extracted, and all services are brought up automatically.

Communication between the container's Apache HTTPD and Spring Boot uses the **AJP (Apache JServ Protocol)** — a binary protocol purpose-built for web server ↔ application server communication. AJP natively carries client connection metadata (remote IP, SSL status, port) without requiring explicit `X-Forwarded-*` header configuration at the container level.

The design supports **multiple clients from a single Docker image** — all client-specific values (S3 bucket/key, AWS credentials, port mappings, application properties) are injected through environment variables at container launch time. Each client container is isolated on its own Docker network to prevent cross-client communication.

---

## 2. Architecture

### 2.1 Two-Layer Apache Design

Traffic flows through two layers of Apache HTTPD — one on the host and one inside each container. The container Apache communicates with Spring Boot over AJP. Each container runs on an isolated Docker network:

```
                         Internet
                            │
                            ▼
┌───────────────────────────────────────────────────────────────┐
│  EC2 Instance                                                 │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Host Apache HTTPD (:443 / :80)                         │  │
│  │                                                         │  │
│  │  SSL termination for all clients                        │  │
│  │  Adds header: X-Forwarded-Proto: https                  │  │
│  │                                                         │  │
│  │  VHost: acme.example.com    → ProxyPass localhost:9001  │  │
│  │  VHost: globex.example.com  → ProxyPass localhost:9002  │  │
│  │  VHost: initech.example.com → ProxyPass localhost:9003  │  │
│  └────────┬──────────────┬──────────────┬──────────────────┘  │
│           │ HTTP         │ HTTP         │ HTTP                 │
│           ▼              ▼              ▼                      │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐          │
│  │ Container A  │ │ Container B  │ │ Container C  │          │
│  │ net: acme    │ │ net: globex  │ │ net: initech │          │
│  │ :9001        │ │ :9002        │ │ :9003        │          │
│  │              │ │              │ │              │          │
│  │ supervisord  │ │ supervisord  │ │ supervisord  │          │
│  │  ├ Apache    │ │  ├ Apache    │ │  ├ Apache    │          │
│  │  │ ├ static  │ │  │ ├ static  │ │  │ ├ static  │          │
│  │  │ └▶AJP──▶ │ │  │ └▶AJP──▶ │ │  │ └▶AJP──▶ │          │
│  │  └ Spring    │ │  └ Spring    │ │  └ Spring    │          │
│  │    Boot:8009 │ │    Boot:8009 │ │    Boot:8009 │          │
│  └──────────────┘ └──────────────┘ └──────────────┘          │
│                                                               │
└───────────────────────────────────────────────────────────────┘
         │
         ▼  (at startup only)
   ┌───────────┐
   │  AWS S3   │  s3://<bucket>/clients/<name>/builds/<version>/
   │  Bucket   │
   └───────────┘
```

**Host Apache HTTPD** is responsible for SSL termination and routing each client's domain to the correct container port via `ProxyPass` (HTTP). It adds `X-Forwarded-Proto: https` so downstream services know the original protocol.

**Container Apache HTTPD** listens on a single configurable HTTP port (mapped to the host), serves static content from the extracted `docs.zip`, and forwards dynamic requests (`.action`, `.jsp`, etc.) to the Spring Boot application over **AJP on port 8009** inside the same container.

**supervisord** manages both Apache HTTPD and Spring Boot as child processes. If either process exits, supervisord restarts it automatically. If both processes fail beyond the configured retry limit, supervisord itself exits, causing Docker to restart the container (via `restart: unless-stopped`).

---

## 3. S3 Archive Format

### 3.1 Bucket Structure

Client releases are stored in a versioned folder hierarchy within S3:

```
s3://your-deploy-bucket/
└── clients/
    ├── acme/
    │   └── builds/
    │       ├── 1.0.0/
    │       │   └── release.tar.gz
    │       ├── 1.1.0/
    │       │   └── release.tar.gz
    │       └── 2.0.0/
    │       │   └── release.tar.gz
    │       └── latest → 2.0.0          # optional: symlink or copy
    ├── globex/
    │   └── builds/
    │       ├── 1.0.0/
    │       │   └── release.tar.gz
    │       └── 1.5.0/
    │           └── release.tar.gz
    └── ...
```

### 3.2 Archive Contents

Each `release.tar.gz` contains:

```
release.tar.gz
├── app.war            # Executable Spring Boot WAR (includes embedded Tomcat + JSPs)
└── docs.zip           # Static site content for container Apache HTTPD (with built JS)
```

The WAR is an executable Spring Boot WAR produced by Gradle's `bootWar` task. It bundles JSPs inside the WAR so Tomcat can compile and serve them at runtime. The docs zip contains the static HTML/CSS/JS content served directly by the container's Apache HTTPD — the JavaScript is pre-built (via Vite/Rollup) during the CI pipeline before packaging.

### 3.3 Versioned Access via AWS Credentials

The container uses the same AWS credentials (`AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY`) to both download the archive at startup **and** run the application at runtime. The S3 path is constructed from the `S3_BUCKET`, `CLIENT_NAME`, and `BUILD_VERSION` environment variables:

```
s3://${S3_BUCKET}/clients/${CLIENT_NAME}/builds/${BUILD_VERSION}/release.tar.gz
```

This approach eliminates pre-signed URLs entirely. Since the credentials don't expire, containers can restart at any time without requiring a fresh URL — Docker's `restart: unless-stopped` just works.

### 3.4 Deployment and Rollback

Deploying a new version or rolling back to a previous version is a matter of changing the `BUILD_VERSION` environment variable and restarting the container:

```bash
# Deploy new version
docker compose up -d --no-deps acme   # after updating BUILD_VERSION in .env

# Rollback to previous version
# 1. Edit .env: ACME_BUILD_VERSION=1.1.0
# 2. Restart:
docker compose up -d --no-deps acme
```

All versions remain in S3 indefinitely (governed by your bucket lifecycle policy), so rollback is always instant — no rebuild required.

### 3.5 IAM Policy

The AWS credentials provided to each container need read access to the client's archive path and whatever runtime services the app uses. A minimal S3 policy for the archive download:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": ["s3:GetObject"],
            "Resource": "arn:aws:s3:::your-deploy-bucket/clients/acme/builds/*"
        }
    ]
}
```

Scope additional permissions (DynamoDB, other S3 buckets, etc.) per client as needed.

---

## 4. Build Pipeline (CodeBuild + Gradle)

Each client project is built via **AWS CodeBuild** using a `buildspec.yml` at the project root. The pipeline compiles the Spring Boot WAR, builds the frontend JavaScript, packages the docs zip, and assembles the final `release.tar.gz` for upload to S3.

### 4.1 Project Structure (expected)

```
<project-root>/
├── build.gradle              # Gradle build with war, bootWar, makeDocs tasks
├── buildspec.yml             # CodeBuild pipeline definition
├── gradlew / gradlew.bat     # Gradle wrapper
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/             # Spring Boot application code
│   │   ├── resources/        # application.properties, templates, etc.
│   │   ├── webapp/           # JSPs (bundled inside the WAR by bootWar)
│   │   └── docs/             # Static site content + JS source
│   │       ├── html/         # Static HTML, CSS, images
│   │       ├── js/           # JS source (built by Vite/Rollup)
│   │       ├── package.json
│   │       ├── yarn.lock
│   │       └── vite.config.js
│   └── test/
└── ...
```

### 4.2 build.gradle (Reference Template)

Each client project needs a `build.gradle` that produces two key artifacts: the executable WAR and the docs zip. Below is a generalized template based on the reference project. Each client project should customize the `group`, `version`, `archiveBaseName`, `mainClass`, dependency list, and source set structure to match their application.

```groovy
plugins {
    id 'java'
    id 'war'
    id 'org.springframework.boot' version '4.0.0'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.yourclient.project'
version = '0.0.1'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

// ------------------------------------------------------------------
// Source sets — customize per project if you have util/proto/sample
// source trees. At minimum, src/main/java is required.
// ------------------------------------------------------------------
sourceSets {
    main {
        java {
            srcDirs = ['src/main/java']
            // Add additional source dirs as needed:
            // srcDirs = ['src/main/java', 'src/util/java']
        }
    }
}

dependencies {
    // --- Spring Boot core ---
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-tomcat'

    // --- JSP support (required for WAR with JSPs) ---
    implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'
    implementation 'jakarta.servlet:jakarta.servlet-api:6.1.0'
    implementation 'jakarta.servlet.jsp:jakarta.servlet.jsp-api:4.0.0'
    implementation 'jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.2'
    implementation 'org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1'

    // --- AWS SDK (add only what this project needs) ---
    implementation 'software.amazon.awssdk:dynamodb:2.21.0'
    implementation 'software.amazon.awssdk:dynamodb-enhanced:2.21.0'
    implementation 'software.amazon.awssdk:s3:2.21.0'

    // --- Add project-specific dependencies below ---
    // implementation '...'

    // --- Test ---
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.withType(JavaCompile).configureEach {
    options.compilerArgs += ['-parameters']
}

springBoot {
    mainClass = 'com.yourclient.project.Application'
}

// ------------------------------------------------------------------
// bootWar — produces the executable WAR with embedded Tomcat + JSPs
// ------------------------------------------------------------------
bootWar {
    archiveBaseName = "${project.name}_Webapp"
    archiveClassifier = ""
}

// ------------------------------------------------------------------
// makeDocs — zips the docs folder for Apache HTTPD, excluding JS
// source/build tooling. The built JS output (from yarn build) is
// included; the source files, node_modules, and configs are excluded.
// ------------------------------------------------------------------
tasks.register('makeDocs', Zip) {
    archiveBaseName = "${project.name}_Docs"
    from("src/main/docs/") {
        exclude("eslint.config.js")
        exclude("js")               // JS source — built output is elsewhere
        exclude("node_modules")
        exclude("package*.json")
        exclude("README.md")
        exclude("rollup.config.js")
        exclude("scripts")
        exclude("static")
        exclude("vite.config.js")
        exclude("yarn.lock")
    }
}

// ------------------------------------------------------------------
// packageRelease — assembles the final release.tar.gz containing
// the WAR and docs zip, ready for upload to S3.
// ------------------------------------------------------------------
tasks.register('packageRelease', Tar) {
    dependsOn bootWar, 'makeDocs'
    archiveBaseName = "release"
    archiveExtension = "tar.gz"
    compression = Compression.GZIP
    destinationDirectory = layout.buildDirectory.dir("release")

    from(bootWar.archiveFile) {
        rename { "app.war" }
    }
    from(tasks.named('makeDocs').flatMap { it.archiveFile }) {
        rename { "docs.zip" }
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**Key points for each client project:**

- `bootWar` produces an **executable WAR** with embedded Tomcat — this is what the container runs via `java -jar app.war`. JSPs are packed inside the WAR and compiled by Tomcat at runtime.
- `makeDocs` zips the `src/main/docs/` folder, excluding JS source files, build configs, and `node_modules`. The Vite/Rollup build output should land somewhere inside `src/main/docs/` (e.g., `src/main/docs/dist/` or `src/main/docs/built/`) before this task runs — the `buildspec.yml` handles build ordering.
- `packageRelease` is a new task that combines the WAR and docs zip into the final `release.tar.gz` with the canonical names (`app.war` and `docs.zip`) expected by the Docker entrypoint.

### 4.3 buildspec.yml (Reference Template)

This `buildspec.yml` is designed to work across all client projects. It uses environment variables configured in each project's CodeBuild settings to control the S3 upload path. Each client's CodeBuild project sets `S3_DEPLOY_BUCKET`, `CLIENT_NAME`, and `BUILD_VERSION` in its environment configuration.

```yaml
version: 0.2

env:
  variables:
    # These should be overridden per-project in CodeBuild environment config.
    # They are listed here as documentation of what's required.
    S3_DEPLOY_BUCKET: "your-deploy-bucket"
    CLIENT_NAME: "your-client"
    BUILD_VERSION: "0.0.0"

phases:
  install:
    runtime-versions:
      java: corretto17
      nodejs: 22
    commands:
      - echo "=== Install Phase ==="
      - chmod +x gradlew
      - ./gradlew --version

  pre_build:
    commands:
      - echo "=== Pre-Build Phase ==="
      # Build the frontend JS (Vite/Rollup) before Gradle packages docs
      - echo "Building frontend JS..."
      - cd src/main/docs
      - yarn install --frozen-lockfile
      - yarn build
      - cd ../../..

  build:
    commands:
      - echo "=== Build Phase ==="
      - java --version
      # Clean, run tests, build WAR, build docs zip, assemble release
      - ./gradlew clean test bootWar makeDocs packageRelease
      - echo "Build complete"
      - ls -la build/release/

  post_build:
    commands:
      - echo "=== Post-Build Phase ==="
      # Upload the release archive to the versioned S3 path
      - >
        aws s3 cp
        build/release/release.tar.gz
        s3://${S3_DEPLOY_BUCKET}/clients/${CLIENT_NAME}/builds/${BUILD_VERSION}/release.tar.gz
        --only-show-errors
      - echo "Uploaded release to s3://${S3_DEPLOY_BUCKET}/clients/${CLIENT_NAME}/builds/${BUILD_VERSION}/"
      # Optionally copy to 'latest' for convenience
      - >
        aws s3 cp
        build/release/release.tar.gz
        s3://${S3_DEPLOY_BUCKET}/clients/${CLIENT_NAME}/builds/latest/release.tar.gz
        --only-show-errors
      - echo "Deploy complete"

artifacts:
  files:
    - build/release/release.tar.gz
  discard-paths: yes
```

### 4.4 CodeBuild Project Configuration

Each client project needs a CodeBuild project with the following settings:

**Environment variables** (set in the CodeBuild project, not in `buildspec.yml`):

| Variable | Example | Description |
|---|---|---|
| `S3_DEPLOY_BUCKET` | `your-deploy-bucket` | S3 bucket for release archives |
| `CLIENT_NAME` | `acme` | Client identifier, matches the S3 folder structure |
| `BUILD_VERSION` | `2.1.0` | Version being built — update per build or derive from Git tag |

**IAM role** — the CodeBuild service role needs:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": ["s3:PutObject"],
            "Resource": "arn:aws:s3:::your-deploy-bucket/clients/*/builds/*"
        }
    ]
}
```

**Source** — connect to the client's Git repository (CodeCommit, GitHub, Bitbucket).

**Build image** — `aws/codebuild/amazonlinux-aarch64-standard:3.0` (or x86 equivalent) with the `amazonlinux2-aarch64-standard:corretto17` runtime.

### 4.5 Build Pipeline Flow

```
Git Push / Manual Trigger
     │
     ▼
CodeBuild Project (per client)
     │
     ├─ install: set up Java 17 + Node 22, verify Gradle
     │
     ├─ pre_build: cd src/main/docs → yarn install → yarn build
     │   (compiles JS via Vite/Rollup into docs build output)
     │
     ├─ build: ./gradlew clean test bootWar makeDocs packageRelease
     │   ├─ bootWar → build/libs/<Project>_Webapp-<version>.war
     │   ├─ makeDocs → build/distributions/<Project>_Docs-<version>.zip
     │   └─ packageRelease → build/release/release.tar.gz
     │       ├─ app.war    (renamed from bootWar output)
     │       └─ docs.zip   (renamed from makeDocs output)
     │
     └─ post_build:
         ├─ aws s3 cp → s3://<bucket>/clients/<name>/builds/<version>/release.tar.gz
         └─ aws s3 cp → s3://<bucket>/clients/<name>/builds/latest/release.tar.gz
```

### 4.6 Adapting an Existing Project

To bring an existing Spring Boot project into this pipeline:

1. **Ensure the project uses the `war` plugin** and `bootWar` task in `build.gradle`. If the project currently uses `bootJar`, switch by adding `id 'war'` to the plugins block. Spring Boot's executable WAR works identically to `bootJar` but also packages JSPs.

2. **Add the `makeDocs` task** to `build.gradle`. Adjust the `from()` path and `exclude()` patterns to match where the project's static content lives and what build tooling files need to be stripped out.

3. **Add the `packageRelease` task** to `build.gradle`. This task depends on `bootWar` and `makeDocs` and produces the canonical `release.tar.gz`.

4. **Move or verify the JS build** in `src/main/docs/`. The project should have a `package.json` with a `build` script that compiles JS via Vite, Rollup, Webpack, or similar. The `buildspec.yml` runs `yarn build` before Gradle so the compiled output is in place when `makeDocs` runs.

5. **Add the `buildspec.yml`** to the project root. The template above works as-is for most projects. Only the CodeBuild environment variables need to be configured per client.

6. **Verify the `src/main/docs/` exclusions** in the `makeDocs` task. The default exclusions cover Vite/Rollup/ESLint config files. If the project uses Webpack or a different bundler, adjust the exclusion list accordingly (e.g., add `webpack.config.js`, `.babelrc`).

---

## 5. Environment Variables

All per-client and per-environment configuration is supplied via environment variables:

| Variable | Required | Description |
|---|---|---|
| `S3_BUCKET` | Yes | S3 bucket name containing the client archives |
| `CLIENT_NAME` | Yes | Client identifier (used in the S3 path, e.g., `acme`) |
| `BUILD_VERSION` | Yes | Version to deploy (e.g., `2.0.0` or `latest`) |
| `AWS_ACCESS_KEY_ID` | Yes | AWS access key — used for both S3 download and app runtime |
| `AWS_SECRET_ACCESS_KEY` | Yes | AWS secret key — used for both S3 download and app runtime |
| `AWS_DEFAULT_REGION` | No | AWS region (default: `us-east-1`) |
| `APP_PROFILE` | No | Spring Boot profile to activate (default: `production`) |
| `AJP_PORT` | No | AJP connector port inside the container (default: `8009`) |
| `AJP_SECRET` | No | Shared secret for AJP authentication (default: `changeit` — **must be overridden in production**) |
| `HTTPD_PORT` | No | Container Apache HTTPD listen port (default: `80`) |
| `HTTPD_SERVER_NAME` | No | Apache `ServerName` directive (default: `localhost`) |
| `JAVA_OPTS` | No | Additional JVM arguments (e.g., `-Xmx512m`) |
| `SQS_LOG_QUEUE_URL` | No | Full SQS queue URL for log shipping. If not set, the log shipper does not start. |

> **Note on AJP_SECRET:** Since Tomcat 9.0.31+ (Ghostcat fix, CVE-2020-1938), AJP connectors require a shared secret by default. The container Apache and the Spring Boot AJP connector must use the same value. Always set a strong, unique secret per environment.

---

## 6. Docker Image

### 6.1 Base Image

```
FROM amazoncorretto:17-al2023
```

Amazon Corretto 17 on Amazon Linux 2023 provides LTS Java 17 and straightforward `yum` access to Apache HTTPD packages.

### 6.2 Installed Packages

- `httpd` (Apache HTTPD 2.4) — static content serving
- `mod_proxy`, `mod_proxy_ajp` — AJP reverse proxy to Spring Boot
- `python3-pip` + `supervisor` — process supervision for Apache and Spring Boot
- `awscli` — S3 archive download via `aws s3 cp`
- `tar`, `unzip` — archive extraction

> **Not needed in the container:** `mod_ssl` (SSL is terminated at the host Apache) or `mod_proxy_http` (container-to-app communication uses AJP, not HTTP).

### 6.3 Dockerfile

```dockerfile
FROM amazoncorretto:17-al2023

# Install Apache HTTPD, supervisord, AWS CLI, and utilities
RUN yum install -y httpd python3-pip tar unzip \
    && pip3 install --break-system-packages supervisor \
    && yum install -y awscli \
    && yum clean all

# Create application and log directories
RUN mkdir -p /opt/app /var/www/html /opt/config /var/log/supervisor /var/log/app /opt/log-shipper

# Copy configuration templates, log shipper, and entrypoint
COPY httpd-container.conf /opt/config/httpd-container.conf
COPY supervisord.conf     /opt/config/supervisord.conf
COPY log-shipper.jar      /opt/log-shipper/log-shipper.jar
COPY entrypoint.sh        /opt/entrypoint.sh
RUN chmod +x /opt/entrypoint.sh

# Container HTTPD port (overridable) — AJP stays internal
EXPOSE 80

ENTRYPOINT ["/opt/entrypoint.sh"]
```

> **Note:** Port 8009 (AJP) is intentionally **not exposed** from the container. It is used only for internal communication between the container Apache and Spring Boot on `localhost`.

---

## 7. Process Supervision with supervisord

supervisord manages three processes inside each container: Apache HTTPD, the Spring Boot application, and a lightweight Java log shipper that forwards log lines to SQS. If any process crashes, supervisord restarts it automatically. If a process fails more than 3 times within 60 seconds, supervisord gives up on it and exits — causing Docker to restart the entire container.

### 7.1 supervisord.conf

```ini
[supervisord]
nodaemon=true
user=root
logfile=/var/log/supervisor/supervisord.log
pidfile=/var/run/supervisord.pid
childlogdir=/var/log/supervisor

[program:httpd]
command=/usr/sbin/httpd -D FOREGROUND
autostart=true
autorestart=true
startretries=3
startsecs=5
stdout_logfile=/var/log/app/httpd_stdout.log
stdout_logfile_maxbytes=10MB
stdout_logfile_backups=3
stderr_logfile=/var/log/app/httpd_stderr.log
stderr_logfile_maxbytes=10MB
stderr_logfile_backups=3
priority=10

[program:springboot]
command=java %(ENV_JAVA_OPTS)s
    -Dspring.profiles.active=%(ENV_APP_PROFILE)s
    -Dtomcat.ajp.port=%(ENV_AJP_PORT)s
    -Dtomcat.ajp.secret=%(ENV_AJP_SECRET)s
    -jar /opt/app/app.war
autostart=true
autorestart=true
startretries=3
startsecs=15
stdout_logfile=/var/log/app/springboot_stdout.log
stdout_logfile_maxbytes=10MB
stdout_logfile_backups=3
stderr_logfile=/var/log/app/springboot_stderr.log
stderr_logfile_maxbytes=10MB
stderr_logfile_backups=3
priority=20
stopwaitsecs=30
stopsignal=TERM

[program:log-shipper]
command=java -jar /opt/log-shipper/log-shipper.jar
    --log-dir=/var/log/app
    --sqs-queue-url=%(ENV_SQS_LOG_QUEUE_URL)s
    --client-name=%(ENV_CLIENT_NAME)s
    --source-tag=%(ENV_HTTPD_SERVER_NAME)s
autostart=true
autorestart=true
startretries=3
startsecs=5
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0
priority=30

[eventlistener:exit_on_fatal]
command=bash -c "while read line; do echo \"READY\"; if echo \"$line\" | grep -q FATAL; then kill -SIGTERM $(cat /var/run/supervisord.pid); fi; echo \"RESULT 2\nOK\"; done"
events=PROCESS_STATE_FATAL
```

**Key design decisions:**

- `nodaemon=true` keeps supervisord in the foreground as PID 1 — Docker monitors this process for container lifecycle.
- Apache starts with `-D FOREGROUND` so supervisord can manage it directly (instead of Apache forking into the background).
- Spring Boot gets `stopwaitsecs=30` to allow graceful shutdown and connection draining.
- The `exit_on_fatal` event listener causes supervisord to exit if any process enters the FATAL state (exceeded retry limit), which triggers Docker's restart policy for the container.
- Apache and Spring Boot logs are written to files under `/var/log/app/` (with rotation), which the log shipper tails and forwards to SQS. The log shipper itself logs to stdout/stderr for Docker-level visibility.
- The log shipper starts with `priority=30` so it comes up after the other services.

---

## 8. Entrypoint Script

`entrypoint.sh` handles the bootstrap sequence (S3 download, extraction, config rendering) and then hands off to supervisord:

```bash
#!/usr/bin/env bash
set -euo pipefail

echo "=== Bootstrap Start ==="

# ------------------------------------------------------------------
# 1. Validate required environment
# ------------------------------------------------------------------
: "${S3_BUCKET:?Error: S3_BUCKET is not set}"
: "${CLIENT_NAME:?Error: CLIENT_NAME is not set}"
: "${BUILD_VERSION:?Error: BUILD_VERSION is not set}"
: "${AWS_ACCESS_KEY_ID:?Error: AWS_ACCESS_KEY_ID is not set}"
: "${AWS_SECRET_ACCESS_KEY:?Error: AWS_SECRET_ACCESS_KEY is not set}"

# ------------------------------------------------------------------
# 2. Download archive from S3 using AWS credentials
# ------------------------------------------------------------------
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-us-east-1}"

S3_PATH="s3://${S3_BUCKET}/clients/${CLIENT_NAME}/builds/${BUILD_VERSION}/release.tar.gz"
ARCHIVE_PATH="/tmp/release.tar.gz"

echo "Downloading archive: ${S3_PATH}"
aws s3 cp "$S3_PATH" "$ARCHIVE_PATH" --only-show-errors

# ------------------------------------------------------------------
# 3. Extract archive
# ------------------------------------------------------------------
echo "Extracting archive..."
mkdir -p /tmp/release-staging
tar -xzf "$ARCHIVE_PATH" -C /tmp/release-staging
mv /tmp/release-staging/app.war /opt/app/app.war

echo "Extracting HTTPD static docs..."
unzip -o /tmp/release-staging/docs.zip -d /var/www/html/

# Clean up
rm -rf "$ARCHIVE_PATH" /tmp/release-staging

# ------------------------------------------------------------------
# 4. Configure container Apache HTTPD
# ------------------------------------------------------------------
export AJP_PORT="${AJP_PORT:-8009}"
export AJP_SECRET="${AJP_SECRET:-changeit}"
export HTTPD_PORT="${HTTPD_PORT:-80}"
export HTTPD_SERVER_NAME="${HTTPD_SERVER_NAME:-localhost}"

# Render the config template with actual values
sed -e "s|__AJP_PORT__|${AJP_PORT}|g" \
    -e "s|__AJP_SECRET__|${AJP_SECRET}|g" \
    -e "s|__HTTPD_PORT__|${HTTPD_PORT}|g" \
    -e "s|__SERVER_NAME__|${HTTPD_SERVER_NAME}|g" \
    /opt/config/httpd-container.conf > /etc/httpd/conf.d/app.conf

# Update the main Listen directive if non-default port
if [ "$HTTPD_PORT" != "80" ]; then
    sed -i "s/^Listen 80$/Listen ${HTTPD_PORT}/" /etc/httpd/conf/httpd.conf
fi

# ------------------------------------------------------------------
# 5. Export env vars for supervisord to pass to Spring Boot
# ------------------------------------------------------------------
export APP_PROFILE="${APP_PROFILE:-production}"
export JAVA_OPTS="${JAVA_OPTS:-}"

# ------------------------------------------------------------------
# 6. Configure log shipper (optional — disabled if SQS_LOG_QUEUE_URL not set)
# ------------------------------------------------------------------
export SQS_LOG_QUEUE_URL="${SQS_LOG_QUEUE_URL:-}"
if [ -z "$SQS_LOG_QUEUE_URL" ]; then
    echo "SQS_LOG_QUEUE_URL not set — log shipper will not start."
    # Remove the log-shipper program from supervisord config so it doesn't try to start
    sed -i '/^\[program:log-shipper\]/,/^$/d' /opt/config/supervisord.conf
fi

# ------------------------------------------------------------------
# 7. Hand off to supervisord
# ------------------------------------------------------------------
echo "Bootstrap complete. Starting supervisord..."
exec /usr/local/bin/supervisord -c /opt/config/supervisord.conf
```

**Key design decisions:**

- The entrypoint handles only the one-time bootstrap work (download, extract, configure). Long-running process management is delegated to supervisord.
- All environment variables are exported so supervisord can reference them via `%(ENV_...)s` syntax.
- `exec` replaces the shell with supervisord, making it PID 1.
- Uses `aws s3 cp` with the container's own AWS credentials — no pre-signed URLs, no expiration, restarts always work.

---

## 9. Spring Boot AJP Configuration

The Spring Boot application must enable an AJP connector on its embedded Tomcat. This is done via a configuration class that reads the properties set by the entrypoint/supervisord:

```java
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AjpConnectorConfig {

    @Value("${tomcat.ajp.port:8009}")
    private int ajpPort;

    @Value("${tomcat.ajp.secret:changeit}")
    private String ajpSecret;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> ajpCustomizer() {
        return factory -> {
            Connector ajpConnector = new Connector("AJP/1.3");
            ajpConnector.setPort(ajpPort);
            ajpConnector.setSecure(false);
            ajpConnector.setAllowTrace(false);
            ajpConnector.setScheme("http");

            AbstractAjpProtocol<?> protocol =
                (AbstractAjpProtocol<?>) ajpConnector.getProtocolHandler();
            protocol.setSecret(ajpSecret);
            protocol.setSecretRequired(true);

            factory.addAdditionalTomcatConnectors(ajpConnector);
        };
    }
}
```

This class should be included in the Spring Boot application's source code. It creates an AJP/1.3 connector alongside the default HTTP connector.

> **Tip:** If you want to keep an HTTP connector available for debugging inside the container (e.g., `curl localhost:8080/actuator/health`), leave `server.port` at its default. If all traffic must go through AJP, set `server.port=-1` to disable the HTTP connector entirely.

---

## 10. Apache HTTPD Configuration

### 10.1 Host Apache (on EC2 — not managed by Docker)

Each client gets a vhost entry on the host Apache that terminates SSL and proxies to the client's container port over HTTP:

```apache
<VirtualHost *:443>
    ServerName acme.example.com

    SSLEngine on
    SSLCertificateFile    /etc/ssl/certs/acme.example.com.crt
    SSLCertificateKeyFile /etc/ssl/private/acme.example.com.key

    # Forward protocol info so the container Apache / AJP chain
    # knows the original request was over SSL
    RequestHeader set X-Forwarded-Proto "https"
    RequestHeader set X-Forwarded-Port "443"

    ProxyPreserveHost On
    ProxyPass         / http://localhost:9001/
    ProxyPassReverse  / http://localhost:9001/
</VirtualHost>

# Redirect HTTP to HTTPS
<VirtualHost *:80>
    ServerName acme.example.com
    Redirect permanent / https://acme.example.com/
</VirtualHost>
```

### 10.2 Container Apache (inside Docker)

`httpd-container.conf` — template baked into the image, rendered by `entrypoint.sh` at startup. Uses `mod_proxy_ajp` to communicate with Spring Boot:

```apache
<VirtualHost *:__HTTPD_PORT__>
    ServerName __SERVER_NAME__

    # Serve static content from the extracted docs.zip
    DocumentRoot /var/www/html

    <Directory /var/www/html>
        Options -Indexes +FollowSymLinks
        AllowOverride None
        Require all granted
    </Directory>

    # ---------------------------------------------------------------
    # AJP reverse proxy to Spring Boot (Tomcat)
    # ---------------------------------------------------------------
    # The 'secret' parameter must match the Tomcat AJP connector's
    # secret (set via -Dtomcat.ajp.secret).
    # ---------------------------------------------------------------

    # JSP and Struts-style actions
    ProxyPassMatch "^/(.*\.action)$" "ajp://localhost:__AJP_PORT__/$1" secret=__AJP_SECRET__
    ProxyPassMatch "^/(.*\.jsp)$"    "ajp://localhost:__AJP_PORT__/$1" secret=__AJP_SECRET__

    # API endpoints
    ProxyPass         /api  ajp://localhost:__AJP_PORT__/api  secret=__AJP_SECRET__
    ProxyPassReverse  /api  ajp://localhost:__AJP_PORT__/api

    # Container-level health check (HTTPD server-status)
    <Location /container-health>
        SetHandler server-status
        Require all granted
    </Location>

    # Log to stdout/stderr for Docker log collection
    ErrorLog  /dev/stderr
    CustomLog /dev/stdout combined
</VirtualHost>
```

---

## 11. Container Networking and Client Isolation

Each client container runs on its own isolated Docker network. This prevents containers from communicating with each other — a compromised or misconfigured container for one client cannot reach another client's container.

The Docker Compose file defines a per-client network with `internal: false` (so the container can reach S3 and other AWS services) but each container is only attached to its own network, not a shared one.

```yaml
networks:
  acme-net:
    driver: bridge
  globex-net:
    driver: bridge
```

Containers are attached to only their own network:

```yaml
services:
  acme:
    networks:
      - acme-net
    # ...
  globex:
    networks:
      - globex-net
    # ...
```

Since each container publishes its port to `localhost` on the host (e.g., `-p 9001:80`), the host Apache can still reach them — but the containers cannot reach each other via Docker networking.

---

## 12. Container Launch Examples

### 12.1 Docker Compose (multiple clients on one host)

```yaml
version: "3.8"

networks:
  acme-net:
    driver: bridge
  globex-net:
    driver: bridge

services:
  acme:
    image: your-registry/website-bootstrap:latest
    ports:
      - "9001:80"
    networks:
      - acme-net
    environment:
      S3_BUCKET: "your-deploy-bucket"
      CLIENT_NAME: "acme"
      BUILD_VERSION: "${ACME_BUILD_VERSION}"
      AWS_ACCESS_KEY_ID: "${ACME_AWS_ACCESS_KEY_ID}"
      AWS_SECRET_ACCESS_KEY: "${ACME_AWS_SECRET_ACCESS_KEY}"
      AWS_DEFAULT_REGION: "us-east-1"
      AJP_SECRET: "${ACME_AJP_SECRET}"
      APP_PROFILE: "production"
      HTTPD_SERVER_NAME: "acme.example.com"
      SQS_LOG_QUEUE_URL: "${SQS_LOG_QUEUE_URL}"
      JAVA_OPTS: "-Xmx512m -Xms256m"
    mem_limit: 1g
    cpus: 1.0
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/container-health"]
      interval: 30s
      timeout: 10s
      retries: 3

  globex:
    image: your-registry/website-bootstrap:latest
    ports:
      - "9002:80"
    networks:
      - globex-net
    environment:
      S3_BUCKET: "your-deploy-bucket"
      CLIENT_NAME: "globex"
      BUILD_VERSION: "${GLOBEX_BUILD_VERSION}"
      AWS_ACCESS_KEY_ID: "${GLOBEX_AWS_ACCESS_KEY_ID}"
      AWS_SECRET_ACCESS_KEY: "${GLOBEX_AWS_SECRET_ACCESS_KEY}"
      AWS_DEFAULT_REGION: "us-west-2"
      AJP_SECRET: "${GLOBEX_AJP_SECRET}"
      APP_PROFILE: "production"
      HTTPD_SERVER_NAME: "globex.example.com"
      SQS_LOG_QUEUE_URL: "${SQS_LOG_QUEUE_URL}"
      JAVA_OPTS: "-Xmx1g -Xms512m"
    mem_limit: 2g
    cpus: 2.0
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/container-health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### 12.2 Environment File

```bash
# .env — credentials and versions per client (not committed to source control)
# File permissions: chmod 600 .env

# Shared logging (all clients use the same SQS queue in dev)
SQS_LOG_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/dev-log-queue

# Acme Corp
ACME_BUILD_VERSION=2.0.0
ACME_AWS_ACCESS_KEY_ID=AKIA...
ACME_AWS_SECRET_ACCESS_KEY=wJal...
ACME_AJP_SECRET=acme-ajp-s3cret-value

# Globex Corporation
GLOBEX_BUILD_VERSION=1.5.0
GLOBEX_AWS_ACCESS_KEY_ID=AKIA...
GLOBEX_AWS_SECRET_ACCESS_KEY=wJal...
GLOBEX_AJP_SECRET=globex-ajp-s3cret-value
```

### 12.3 Docker Run (single client)

```bash
docker network create acme-net

docker run -d \
  --name acme-webserver \
  --network acme-net \
  -p 9001:80 \
  --memory=1g --cpus=1.0 \
  -e S3_BUCKET="your-deploy-bucket" \
  -e CLIENT_NAME="acme" \
  -e BUILD_VERSION="2.0.0" \
  -e AWS_ACCESS_KEY_ID="AKIA..." \
  -e AWS_SECRET_ACCESS_KEY="wJal..." \
  -e AWS_DEFAULT_REGION="us-east-1" \
  -e AJP_SECRET="s3cur3-r4ndom-str1ng" \
  -e APP_PROFILE="production" \
  -e HTTPD_SERVER_NAME="acme.example.com" \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  your-registry/website-bootstrap:latest
```

---

## 13. Deployment and Rollback Workflow

### 13.1 Deploying a New Version

1. **Build** the new `release.tar.gz` via CodeBuild (runs `./gradlew clean test bootWar makeDocs packageRelease` and uploads to S3 automatically). Or manually:
2. **Upload** to S3 under the new version folder:
   ```bash
   aws s3 cp release.tar.gz s3://your-deploy-bucket/clients/acme/builds/2.1.0/release.tar.gz
   ```
3. **Update** the `.env` file:
   ```bash
   ACME_BUILD_VERSION=2.1.0
   ```
4. **Restart** the container:
   ```bash
   docker compose up -d --no-deps acme
   ```

### 13.2 Rolling Back

1. **Update** the `.env` file to the previous version:
   ```bash
   ACME_BUILD_VERSION=2.0.0
   ```
2. **Restart** the container:
   ```bash
   docker compose up -d --no-deps acme
   ```

Rollback is instant because the previous archive is still in S3 — no rebuild required.

### 13.3 Version Inventory

To list available versions for a client:

```bash
aws s3 ls s3://your-deploy-bucket/clients/acme/builds/
```

### 13.4 Cleanup Policy

Old versions accumulate in S3 over time. Use an S3 lifecycle rule or a manual cleanup script to remove versions older than a retention window (e.g., keep the last 10 versions or the last 90 days). Do not delete the currently deployed version — check the `.env` file before cleanup.

---

## 14. Startup Sequence

```
Container Start
     │
     ▼
entrypoint.sh:
     │
     ├─ Validate env vars (S3_BUCKET, CLIENT_NAME, BUILD_VERSION, AWS creds)
     │    fail → exit 1
     │
     ├─ aws s3 cp → download release.tar.gz
     │    fail → exit 1
     │
     ├─ Extract app.war → /opt/app/
     │  Extract docs.zip → /var/www/html/
     │
     ├─ Render container Apache config (with AJP secret)
     │
     └─ exec supervisord
              │
              ├─ Start Apache HTTPD (-D FOREGROUND)
              │    crash → auto-restart (up to 3 retries)
              │
              └─ Start Spring Boot WAR (via java -jar app.war)
                   crash → auto-restart (up to 3 retries)
                   │
                   ▼
              Container ready — HTTPD serving on configured port
```

---

## 15. Request Flow Example

A request to `https://acme.example.com/docs/guide.html` (static content):

1. **Host Apache** receives the HTTPS request on `:443`, terminates SSL, adds `X-Forwarded-Proto: https`, and proxies to `http://localhost:9001/`.
2. **Container Apache** (listening on `:80`, mapped to host `:9001`) matches `guide.html` as static content under `/var/www/html/docs/` and serves it directly. Spring Boot is never involved.

A request to `https://acme.example.com/login.action` (dynamic content):

1. **Host Apache** terminates SSL, adds `X-Forwarded-Proto: https`, proxies to `http://localhost:9001/login.action`.
2. **Container Apache** matches `*.action` via `ProxyPassMatch` and forwards to `ajp://localhost:8009/login.action` with the configured secret.
3. **Spring Boot (Tomcat AJP connector)** receives the request. AJP natively carries the client's remote IP, protocol (HTTPS), and server port — Tomcat populates `request.isSecure()`, `request.getRemoteAddr()`, and `request.getScheme()` automatically without relying on `X-Forwarded-*` header parsing.

---

## 16. Logging Architecture

Each container runs a lightweight Java log shipper as a third supervisord process. The shipper tails the Apache and Spring Boot log files and sends each line as a message to an SQS queue, tagged with the client name and source hostname. A separate consumer service (out of scope for this document) reads from the queue and stores/processes the logs.

### 16.1 Log Flow

```
┌─────────────────────────────────────────────────────┐
│  Container                                           │
│                                                      │
│  Apache HTTPD ──► /var/log/app/httpd_stdout.log      │
│                   /var/log/app/httpd_stderr.log      │
│                                                      │──► Log Shipper ──► SQS Queue
│  Spring Boot ──► /var/log/app/springboot_stdout.log  │
│                  /var/log/app/springboot_stderr.log   │
│                                                      │
└─────────────────────────────────────────────────────┘
```

### 16.2 How It Works

Apache and Spring Boot write their logs to files under `/var/log/app/` (managed by supervisord's file-based log rotation — 10 MB per file, 3 backups). The log shipper process tails all `*.log` files in that directory and, for each new line, sends an SQS message containing the log line, the client name, the source file (to distinguish Apache vs. Spring Boot), and a timestamp.

### 16.3 SQS Message Format

Each SQS message body is a JSON object:

```json
{
    "clientName": "acme",
    "sourceTag": "acme.example.com",
    "sourceFile": "springboot_stdout.log",
    "timestamp": "2026-04-04T14:32:01.123Z",
    "line": "2026-04-04 14:32:01.123 INFO  [main] com.acme.Application - Started in 4.2s"
}
```

### 16.4 Log Shipper Implementation

The log shipper is a small standalone Java application (separate from the client's Spring Boot app) that ships as a pre-built JAR baked into the Docker image at `/opt/log-shipper/log-shipper.jar`. It uses the AWS SQS SDK and accepts configuration via command-line arguments:

| Argument | Description |
|---|---|
| `--log-dir` | Directory to tail (default: `/var/log/app`) |
| `--sqs-queue-url` | Full SQS queue URL |
| `--client-name` | Client identifier included in each message |
| `--source-tag` | Hostname/domain tag for the source container |

The shipper uses the container's `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables (already present for the Spring Boot app) to authenticate with SQS. The IAM policy for the client's credentials needs an additional permission:

```json
{
    "Effect": "Allow",
    "Action": ["sqs:SendMessage"],
    "Resource": "arn:aws:sqs:us-east-1:123456789012:dev-log-queue"
}
```

### 16.5 Batching and Resilience

To avoid per-line SQS API calls, the log shipper should batch messages using SQS's `SendMessageBatch` API (up to 10 messages per batch, flushed every 5 seconds or when the batch is full). If SQS is unreachable, the shipper buffers lines in memory (capped at a configurable limit, e.g., 10,000 lines) and retries with exponential backoff. If the buffer overflows, the oldest lines are dropped — log loss is acceptable in a dev environment to avoid blocking the shipper or consuming excessive memory.

### 16.6 Shared vs. Per-Client SQS Queue

For a dev environment, a **single shared SQS queue** for all clients is simplest. The `clientName` field in each message allows the consumer to filter or route by client. If log volume grows or you need per-client access control, you can switch to per-client queues by making `SQS_LOG_QUEUE_URL` client-specific in the `.env` file.

---

## 17. Security Considerations

1. **AWS credentials in environment variables.** `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` are visible in `docker inspect`. For higher security in production, consider migrating to Docker secrets, IAM Instance Roles, or AWS Secrets Manager.
2. **Least-privilege IAM policy.** Each client's AWS credentials should be scoped to only the S3 paths and AWS services that client's application needs. Never share credentials across clients.
3. **No secrets baked into the image.** The Docker image contains no credentials or client-specific data — everything is injected at runtime.
4. **SSL terminates at the host.** Container traffic between host Apache and the Docker container travels over `localhost` HTTP — standard for same-host reverse proxies.
5. **AJP secret required.** The AJP connector requires a shared secret (Tomcat 9.0.31+ / Ghostcat fix). Always override the default `changeit` value in production.
6. **AJP port not exposed.** Port 8009 is not published from the container — AJP is only used internally on `localhost`.
7. **Client network isolation.** Each container runs on its own Docker network. Containers cannot communicate with each other.
8. **Resource limits.** Each container has `mem_limit` and `cpus` set to prevent one client from starving others.
9. **`.env` file security.** The `.env` file contains secrets and must not be committed to version control. Set file permissions to `600` (owner-only read/write).

---

## 18. Onboarding a New Client

Adding a new client requires changes in three places:

### 18.1 S3

Upload the client's first release:

```bash
aws s3 cp release.tar.gz s3://your-deploy-bucket/clients/<client-name>/builds/1.0.0/release.tar.gz
```

### 18.2 Docker Compose + .env

Add a new service block to `docker-compose.yml` with a unique port and isolated network, and add the client's credentials and version to `.env`.

### 18.3 Host Apache

Add a new vhost entry for the client's domain with SSL cert and `ProxyPass` to the container's port. Reload Apache:

```bash
sudo systemctl reload httpd
```

---

## 19. Open Questions / Future Iterations

- **Health checks:** Should the host Apache health endpoint verify the container is healthy end-to-end (Apache + AJP + Spring Boot), or just confirm the container Apache responds? Consider adding a `ProxyPass /app-health ajp://localhost:8009/actuator/health` for deep checks.
- **Additional proxy patterns:** Are there URL patterns beyond `.action`, `.jsp`, and `/api` that should be forwarded to Spring Boot via AJP?
- **HTTP connector:** Should the Spring Boot HTTP connector (`:8080`) be left active for in-container debugging, or disabled entirely with `server.port=-1`?
- **SSL cert automation:** Should Let's Encrypt / certbot be used on the host Apache for automated certificate management?
- **Log shipper implementation:** The log shipper JAR needs to be built as a separate project. Define the repository and build pipeline for it.
- **SQS consumer:** The consumer service that reads from the SQS log queue and stores/processes logs needs to be designed separately.
