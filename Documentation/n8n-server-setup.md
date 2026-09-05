# n8n server setup: Docker n8n + native PostgreSQL on one EC2 instance

Plan for a self-hosted n8n instance running as Docker containers on an EC2
box, with PostgreSQL installed directly on the host (for now), inside the
website VPC, behind the existing ALB and WAF, TLS terminated at the ALB, and
the editor reachable only from an admin public IP. Designed to be as tight as
a single-instance deployment reasonably can be. Written 2026-09-05 against the
account state at that time; nothing here has been applied yet.

## 1. Decisions and current state

**Why Docker for n8n.** n8n 3.0 (scheduled October 2026) drops npm installs
and is Docker-only. Running the official image now means the upgrade path is
a tag bump. It also unlocks the production-recommended task runner layout,
where user code from Code nodes runs in a separate sidecar container instead
of inside the n8n process.

**Why PostgreSQL on the host.** Keeps cost and moving parts down for a
single-admin instance. The design keeps the move to RDS a two-line change
(database host and security group) when you want it.

**What exists today (account 148768123182, us-west-2):**

- `prod-webapp-website` CloudFormation stack: hardened VPC `vpc-0ce93749d51183347`
  with public subnets a/b, private subnets a/b, a NAT gateway, ALB
  `prod-webapp-website-alb`, and an ASG of AL2023 instances in the private
  subnets reached only through SSM Session Manager. Templates live in
  `s3://us-west-2.build.thebridgeto.ai/infrastructure/`.
- `prod-waf-website` stack: WAFv2 web ACL on that ALB with the AWS Common,
  Known Bad Inputs and SQLi rule sets plus a 2000 req / 5 min per-IP rate
  limit, logging to CloudWatch.
- `prod-n8n-app` (`i-05042d1f1a1fc8cad`): t4g.medium launched 2026-09-05 via
  the console wizard into the **default** VPC with a public Elastic IP, SSH and
  443 open to the world, no IAM role, an 8 GB root disk, and Node.js 22
  installed. Reachable with `ssh prod-n8n-app`.

**Recommendation: relaunch into the website VPC.** The wizard instance is in
the wrong VPC to be an ALB target and has nothing on it worth keeping. The new
instance lives in a private subnet with no public IP, no SSH, an encrypted
30 GB disk, and an IAM role. Appendix A covers keeping the current box if you
prefer.

**Versions pinned in this plan (current on 2026-09-05):**

| Component | Version | Notes |
|---|---|---|
| n8n image | `docker.n8n.io/n8nio/n8n:2.37.10` | arm64 build available |
| Task runner image | `docker.n8n.io/n8nio/runners:2.37.10` | must match the n8n version exactly |
| Docker Engine | AL2023 `docker` package | Compose plugin is not packaged; installed from the docker/compose release with checksum verification |
| Docker Compose | v5.5.1 | |
| PostgreSQL | 17, AL2023 `postgresql17-server` | |

**Approximate incremental monthly cost (us-west-2 on demand):** t4g.medium
~$25, 30 GB gp3 ~$2.50, NAT data ~$1. ACM, listener rules, Route 53 and WAF
add nothing on top of what the website already pays.

## 2. Security design

Each layer and the control that enforces it. Items marked *optional* are
suggestions in section 8, not in the base install.

| Layer | Control |
|---|---|
| Internet edge | Route 53 alias to the existing ALB. HTTP redirects to HTTPS. ACM certificate for `n8n.thebridgeto.ai` served via SNI. WAF managed rule sets and per-IP rate limit apply to every request. |
| Admin access to the editor | ALB listener rules on the n8n host name: webhook and form paths forward for anyone; everything else forwards only from the admin CIDR and otherwise gets a 403 from the ALB, so unauthenticated visitors never reach n8n's login page. |
| Application login | n8n owner account with two-factor authentication. Public REST API disabled. Community node installs disabled. Templates gallery and telemetry off. |
| Network placement | Private subnet, no public IP. Security group ingress: port 5678 from the ALB security group only. Egress: TCP 443 only (dnf, image pulls, SSM, S3, SES, any HTTPS API). No SSH rule, no key pair, sshd disabled. |
| Instance metadata | IMDSv2 required with hop limit 1, so containers cannot reach the instance role credentials at all. Only host processes (backup job) can. |
| Host | AL2023 arm64, encrypted gp3 root volume, unattended security updates via dnf-automatic, admin shell only through SSM Session Manager. No users in the docker group; the daemon socket is root-only. |
| Docker daemon | `no-new-privileges` default, inter-container communication off on the default bridge, userland proxy off, bounded json-file logs, live-restore. Published port bound to the instance private IP only. |
| n8n container | Runs as the image's non-root user (uid 1000). Read-only root filesystem with tmpfs for `/tmp` and the UI cache. All Linux capabilities dropped. PID and memory limits. Only the `.n8n` data directory is writable, on the encrypted volume. |
| Task runner container | Separate `n8nio/runners` sidecar in external mode: Code node scripts never run in the n8n process and have no access to n8n's credentials store, filesystem, or the host database. Same hardening as the n8n container, no volumes, not published. Broker is reachable only on the private Docker network with a shared auth token. |
| Database | PostgreSQL listens on loopback and the Docker bridge gateway address only, never on the instance IP. `pg_hba` allows the `n8n` role to the `n8n` database from the container subnet with SCRAM only. The role has no superuser, createdb or createrole rights. Connection logging on. |
| Secrets | Encryption key, database password and runner token generated locally and stored as SSM SecureString parameters. On the host they exist only as root-owned files under `/etc/n8n` that Docker mounts into the container as secrets, so they never appear in `docker inspect` or the compose file. `N8N_BLOCK_ENV_ACCESS_IN_NODE` keeps workflow code from reading the environment. |
| Abuse limits | Webhook payload cap 1 MB, form upload cap 1 MB, execution timeout 5 min, execution history pruned at 7 days / 10k rows. Execute Command and Local File Trigger nodes stay disabled (n8n 2.x default). |
| Backups | Nightly `pg_dump` plus a tarball of the data directory to a private, encrypted, versioned S3 bucket with a 35-day lifecycle. Instance role can only write its own prefix. Restore needs the SSM encryption key, which is backed up by SSM itself. |
| Supply chain | Image tags pinned to an exact version; digest recorded after the first pull (pin by digest as an *optional* step). Compose binary verified against the published SHA-256. |

Threats this design does not fully cover: a vulnerability in n8n itself
exploited through a public webhook (mitigated by WAF, size limits, runner
isolation and the container hardening, not eliminated), and a compromise of
your admin workstation or AWS account. Section 8 lists the next steps if you
want to push further.

## 3. Target architecture

```
Admin browser (ADMIN_CIDR) ─┐
Static site form POST      ─┼─▶ Route 53 n8n.thebridgeto.ai ─▶ ALB prod-webapp-website-alb :443 (ACM, SNI)
                             │        │ WAF prod-waf-website
                             │        │ rules for host n8n.thebridgeto.ai:
                             │        │   10  /webhook/* /webhook-waiting/* /form/*  → prod-n8n-tg
                             │        │   20  source-ip ADMIN_CIDR                   → prod-n8n-tg
                             │        │   30  anything else                          → 403
                             │        ▼
                             │   prod-n8n-tg  HTTP :5678  health /healthz
                             │        ▼
                             │   EC2 prod-n8n-app  t4g.medium AL2023 arm64, private subnet, no public IP, IMDSv2 hop limit 1
                             │     ├─ docker network n8n-net 172.30.0.0/24 (gateway 172.30.0.1)
                             │     │    ├─ container n8n      (n8nio/n8n:2.37.10)  published PRIVATE_IP:5678 → 5678
                             │     │    │     broker :5679 (network-internal only)
                             │     │    └─ container runners (n8nio/runners:2.37.10) → http://n8n:5679
                             │     ├─ PostgreSQL 17 on the host, listening on 127.0.0.1 and 172.30.0.1 only
                             │     ├─ /var/lib/n8n/data  (bind-mounted as /home/node/.n8n, uid 1000, mode 700)
                             │     └─ nightly backup timer → s3://us-west-2.backup.thebridgeto.ai/n8n/
                             └─ SSM Session Manager for shell access (no SSH)
```

## 4. AWS resources (run from your Mac)

Set once per shell:

```bash
export AWS_PROFILE=claude_prod_thebridgeto_ai
export AWS_REGION=us-west-2
export AWS_PAGER=""

VPC_ID=vpc-0ce93749d51183347
SUBNET_ID=subnet-047d840948be8f071   # prod-webapp-website-private-a
ALB_SG=sg-01a703cd410bb7e49           # website ALB security group
ALB_ARN=arn:aws:elasticloadbalancing:us-west-2:148768123182:loadbalancer/app/prod-webapp-website-alb/c5fc450ab39fa1be
LISTENER_443=arn:aws:elasticloadbalancing:us-west-2:148768123182:listener/app/prod-webapp-website-alb/c5fc450ab39fa1be/9b7a1eb2529457fe
ZONE_ID=Z00041362HB0V7RN0S0GA         # thebridgeto.ai hosted zone
N8N_HOST=n8n.thebridgeto.ai
BACKUP_BUCKET=us-west-2.backup.thebridgeto.ai
ADMIN_CIDR="$(curl -s https://checkip.amazonaws.com)/32"
echo "Admin CIDR: $ADMIN_CIDR"
```

### 4.1 Secrets in SSM Parameter Store

Values are generated inline so they never land in shell history. Losing
`/prod/n8n/encryption-key` makes every credential stored in n8n unrecoverable,
so treat SSM as its source of truth.

```bash
aws ssm put-parameter --name /prod/n8n/encryption-key    --type SecureString --value "$(openssl rand -hex 32)"
aws ssm put-parameter --name /prod/n8n/db-password       --type SecureString --value "$(openssl rand -base64 48 | tr -d '/+=' | cut -c1-40)"
aws ssm put-parameter --name /prod/n8n/runners-auth-token --type SecureString --value "$(openssl rand -hex 32)"
```

### 4.2 Backup bucket

Private, encrypted at rest, versioned so an attacker with the instance role
cannot silently overwrite history, 35-day expiry.

```bash
aws s3api create-bucket --bucket $BACKUP_BUCKET --create-bucket-configuration LocationConstraint=us-west-2
aws s3api put-public-access-block --bucket $BACKUP_BUCKET --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
aws s3api put-bucket-encryption --bucket $BACKUP_BUCKET --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"},"BucketKeyEnabled":true}]}'
aws s3api put-bucket-versioning --bucket $BACKUP_BUCKET --versioning-configuration Status=Enabled
aws s3api put-bucket-lifecycle-configuration --bucket $BACKUP_BUCKET --lifecycle-configuration '{"Rules":[{"ID":"expire-n8n-backups","Status":"Enabled","Filter":{"Prefix":"n8n/"},"Expiration":{"Days":35},"NoncurrentVersionExpiration":{"NoncurrentDays":14}}]}'
```

### 4.3 IAM role and instance profile

SSM core for Session Manager, read the three parameters, write (never
delete) under the backup prefix.

```bash
aws iam create-role --role-name prod-n8n-app-role --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}'
aws iam attach-role-policy --role-name prod-n8n-app-role --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore
aws iam put-role-policy --role-name prod-n8n-app-role --policy-name N8nAppAccess --policy-document '{
  "Version":"2012-10-17",
  "Statement":[
    {"Effect":"Allow","Action":["ssm:GetParameter","ssm:GetParameters"],"Resource":"arn:aws:ssm:us-west-2:148768123182:parameter/prod/n8n/*"},
    {"Effect":"Allow","Action":["s3:PutObject"],"Resource":"arn:aws:s3:::us-west-2.backup.thebridgeto.ai/n8n/*"},
    {"Effect":"Allow","Action":["s3:ListBucket"],"Resource":"arn:aws:s3:::us-west-2.backup.thebridgeto.ai","Condition":{"StringLike":{"s3:prefix":["n8n/*"]}}}
  ]}'
aws iam create-instance-profile --instance-profile-name prod-n8n-app-profile
aws iam add-role-to-instance-profile --instance-profile-name prod-n8n-app-profile --role-name prod-n8n-app-role
sleep 15
```

Workflows that call AWS (SES, DynamoDB) cannot use this role: the container
is deliberately cut off from instance metadata, and n8n's AWS credential type
needs an access key anyway. When needed, create a dedicated IAM user with
only those permissions and store its keys in n8n's credential store.

### 4.4 Security group

Ingress 5678 from the ALB only. Egress HTTPS only. DNS and time sync use
link-local addresses that security groups do not filter.

```bash
N8N_SG=$(aws ec2 create-security-group --group-name prod-n8n-app-sg --description "n8n app: 5678 from website ALB, egress 443 only" --vpc-id $VPC_ID --query GroupId --output text)
aws ec2 create-tags --resources $N8N_SG --tags Key=Name,Value=prod-n8n-app-sg
aws ec2 authorize-security-group-ingress --group-id $N8N_SG --ip-permissions "IpProtocol=tcp,FromPort=5678,ToPort=5678,UserIdGroupPairs=[{GroupId=$ALB_SG,Description=website ALB}]"
aws ec2 revoke-security-group-egress   --group-id $N8N_SG --ip-permissions 'IpProtocol=-1,IpRanges=[{CidrIp=0.0.0.0/0}]'
aws ec2 authorize-security-group-egress --group-id $N8N_SG --ip-permissions 'IpProtocol=tcp,FromPort=443,ToPort=443,IpRanges=[{CidrIp=0.0.0.0/0,Description=HTTPS egress}]'
```

If a workflow ever needs SMTP instead of the SES API, add TCP 587 egress then.

### 4.5 ACM certificate on the existing listener

```bash
CERT_ARN=$(aws acm request-certificate --domain-name $N8N_HOST --validation-method DNS --query CertificateArn --output text)
sleep 10
read CNAME_NAME CNAME_VALUE <<< "$(aws acm describe-certificate --certificate-arn $CERT_ARN --query 'Certificate.DomainValidationOptions[0].ResourceRecord.[Name,Value]' --output text)"
aws route53 change-resource-record-sets --hosted-zone-id $ZONE_ID --change-batch "{\"Changes\":[{\"Action\":\"UPSERT\",\"ResourceRecordSet\":{\"Name\":\"$CNAME_NAME\",\"Type\":\"CNAME\",\"TTL\":300,\"ResourceRecords\":[{\"Value\":\"$CNAME_VALUE\"}]}}]}"
aws acm wait certificate-validated --certificate-arn $CERT_ARN
aws elbv2 add-listener-certificates --listener-arn $LISTENER_443 --certificates CertificateArn=$CERT_ARN
```

Check the listener's TLS policy while you are here and move it to
`ELBSecurityPolicy-TLS13-1-2-2021-06` if it is older. That applies to the
website too, which is a good thing.

```bash
aws elbv2 describe-listeners --listener-arns $LISTENER_443 --query 'Listeners[0].SslPolicy' --output text
```

### 4.6 Target group

```bash
TG_ARN=$(aws elbv2 create-target-group --name prod-n8n-tg --protocol HTTP --port 5678 --vpc-id $VPC_ID --target-type instance \
  --health-check-path /healthz --health-check-interval-seconds 30 --healthy-threshold-count 2 --unhealthy-threshold-count 3 --matcher HttpCode=200 \
  --query 'TargetGroups[0].TargetGroupArn' --output text)
aws elbv2 modify-target-group-attributes --target-group-arn $TG_ARN --attributes Key=deregistration_delay.timeout_seconds,Value=30
```

### 4.7 Launch the instance

Latest AL2023 arm64 AMI, private subnet, no public IP, no key pair, encrypted
root volume, IMDSv2 required with hop limit 1.

```bash
AMI_ID=$(aws ssm get-parameter --name /aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64 --query Parameter.Value --output text)
INSTANCE_ID=$(aws ec2 run-instances --image-id $AMI_ID --instance-type t4g.medium \
  --subnet-id $SUBNET_ID --security-group-ids $N8N_SG --no-associate-public-ip-address \
  --iam-instance-profile Name=prod-n8n-app-profile \
  --metadata-options HttpTokens=required,HttpEndpoint=enabled,HttpPutResponseHopLimit=1 \
  --block-device-mappings '[{"DeviceName":"/dev/xvda","Ebs":{"VolumeSize":30,"VolumeType":"gp3","Encrypted":true,"DeleteOnTermination":true}}]' \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=prod-n8n-app}]' 'ResourceType=volume,Tags=[{Key=Name,Value=prod-n8n-app}]' \
  --query 'Instances[0].InstanceId' --output text)
echo $INSTANCE_ID
aws ec2 wait instance-running --instance-ids $INSTANCE_ID
aws elbv2 register-targets --target-group-arn $TG_ARN --targets Id=$INSTANCE_ID
```

### 4.8 Listener rules (the admin lock)

Priorities 10, 20 and 30 are free on this listener. Add `/form-waiting/*` to
rule 10 only if you use n8n Form nodes with a Wait step.

```bash
aws elbv2 create-rule --listener-arn $LISTENER_443 --priority 10 \
  --conditions "[{\"Field\":\"host-header\",\"HostHeaderConfig\":{\"Values\":[\"$N8N_HOST\"]}},{\"Field\":\"path-pattern\",\"PathPatternConfig\":{\"Values\":[\"/webhook/*\",\"/webhook-waiting/*\",\"/form/*\"]}}]" \
  --actions "[{\"Type\":\"forward\",\"TargetGroupArn\":\"$TG_ARN\"}]"

aws elbv2 create-rule --listener-arn $LISTENER_443 --priority 20 \
  --conditions "[{\"Field\":\"host-header\",\"HostHeaderConfig\":{\"Values\":[\"$N8N_HOST\"]}},{\"Field\":\"source-ip\",\"SourceIpConfig\":{\"Values\":[\"$ADMIN_CIDR\"]}}]" \
  --actions "[{\"Type\":\"forward\",\"TargetGroupArn\":\"$TG_ARN\"}]"

aws elbv2 create-rule --listener-arn $LISTENER_443 --priority 30 \
  --conditions "[{\"Field\":\"host-header\",\"HostHeaderConfig\":{\"Values\":[\"$N8N_HOST\"]}}]" \
  --actions '[{"Type":"fixed-response","FixedResponseConfig":{"StatusCode":"403","ContentType":"text/plain","MessageBody":"Forbidden"}}]'
```

### 4.9 DNS

```bash
ALB_DNS=$(aws elbv2 describe-load-balancers --load-balancer-arns $ALB_ARN --query 'LoadBalancers[0].DNSName' --output text)
ALB_ZONE=$(aws elbv2 describe-load-balancers --load-balancer-arns $ALB_ARN --query 'LoadBalancers[0].CanonicalHostedZoneId' --output text)
aws route53 change-resource-record-sets --hosted-zone-id $ZONE_ID --change-batch "{\"Changes\":[{\"Action\":\"UPSERT\",\"ResourceRecordSet\":{\"Name\":\"$N8N_HOST\",\"Type\":\"A\",\"AliasTarget\":{\"HostedZoneId\":\"$ALB_ZONE\",\"DNSName\":\"$ALB_DNS\",\"EvaluateTargetHealth\":false}}}]}"
```

### 4.10 Decommission the wizard instance

Destructive. Only after section 6 passes.

```bash
aws ec2 terminate-instances --instance-ids i-05042d1f1a1fc8cad
aws ec2 wait instance-terminated --instance-ids i-05042d1f1a1fc8cad
aws ec2 release-address --allocation-id eipalloc-03f2798c439a7b37c
aws ec2 delete-security-group --group-id sg-0213802cbfab2154d
aws ec2 delete-key-pair --key-name prod-n8n-app
```

Three other Elastic IPs in us-west-2 are unattached and billing hourly
(34.215.17.116, 44.235.71.20, 54.213.252.5). Unrelated to n8n; release them if
nothing needs them.

## 5. Server install (run inside the instance)

Install the Session Manager plugin on the Mac if missing
(`brew install --cask session-manager-plugin`), then:

```bash
aws ssm start-session --target $INSTANCE_ID
sudo -i
```

Run the script below as root. It is safe to re-run. All secrets come from SSM
through the instance role; nothing is typed in.

```bash
#!/bin/bash
# install-n8n.sh -- Docker n8n + external task runners + native PostgreSQL 17 on AL2023 arm64
set -euo pipefail

N8N_VERSION="${N8N_VERSION:-2.37.10}"
COMPOSE_VERSION="${COMPOSE_VERSION:-v5.5.1}"
REGION=us-west-2
N8N_PUBLIC_URL="https://n8n.thebridgeto.ai/"
TZ_NAME="America/New_York"
NET_SUBNET=172.30.0.0/24
NET_GW=172.30.0.1

# Private IP from IMDSv2 (host is allowed; containers are not, hop limit 1)
IMDS_TOKEN=$(curl -sX PUT http://169.254.169.254/latest/api/token -H "X-aws-ec2-metadata-token-ttl-seconds: 60")
HOST_IP=$(curl -s -H "X-aws-ec2-metadata-token: $IMDS_TOKEN" http://169.254.169.254/latest/meta-data/local-ipv4)
echo "host private ip: $HOST_IP"

echo "== os packages and hardening"
dnf -y update
dnf -y install docker postgresql17 postgresql17-server postgresql17-contrib dnf-automatic
systemctl disable --now sshd || true          # SSM only
sed -i 's/^apply_updates = .*/apply_updates = yes/; s/^upgrade_type = .*/upgrade_type = security/' /etc/dnf/automatic.conf
systemctl enable --now dnf-automatic.timer

echo "== docker engine"
install -d -m 755 /etc/docker
cat > /etc/docker/daemon.json <<'EOF'
{
  "log-driver": "json-file",
  "log-opts": { "max-size": "20m", "max-file": "5" },
  "live-restore": true,
  "no-new-privileges": true,
  "icc": false,
  "userland-proxy": false
}
EOF
systemctl enable --now docker

echo "== docker compose plugin (checksum verified)"
install -d -m 755 /usr/local/lib/docker/cli-plugins
curl -fsSL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-aarch64" -o /usr/local/lib/docker/cli-plugins/docker-compose
curl -fsSL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-aarch64.sha256" -o /tmp/compose.sha256
( cd /usr/local/lib/docker/cli-plugins && awk '{print $1"  docker-compose"}' /tmp/compose.sha256 | sha256sum -c - )
chmod 755 /usr/local/lib/docker/cli-plugins/docker-compose
docker compose version

echo "== docker network with a fixed subnet (postgres binds to its gateway)"
docker network inspect n8n-net >/dev/null 2>&1 || docker network create --driver bridge --subnet "$NET_SUBNET" --gateway "$NET_GW" n8n-net

echo "== secrets from SSM"
DB_PASS=$(aws ssm get-parameter --region $REGION --name /prod/n8n/db-password        --with-decryption --query Parameter.Value --output text)
ENC_KEY=$(aws ssm get-parameter --region $REGION --name /prod/n8n/encryption-key     --with-decryption --query Parameter.Value --output text)
RUNNER_TOKEN=$(aws ssm get-parameter --region $REGION --name /prod/n8n/runners-auth-token --with-decryption --query Parameter.Value --output text)

echo "== postgresql"
[ -f /var/lib/pgsql/data/PG_VERSION ] || postgresql-setup --initdb
cat > /var/lib/pgsql/data/pg_hba.conf <<EOF
# TYPE  DATABASE  USER      ADDRESS          METHOD
local   all       postgres                   peer
local   all       all                        scram-sha-256
host    n8n       n8n       127.0.0.1/32     scram-sha-256
host    n8n       n8n       ${NET_SUBNET}    scram-sha-256
EOF
chown postgres:postgres /var/lib/pgsql/data/pg_hba.conf && chmod 600 /var/lib/pgsql/data/pg_hba.conf
# start after docker so the bridge gateway address exists to bind to
install -d /etc/systemd/system/postgresql.service.d
printf '[Unit]\nAfter=docker.service\nWants=docker.service\n' > /etc/systemd/system/postgresql.service.d/after-docker.conf
systemctl daemon-reload
systemctl enable --now postgresql
sudo -u postgres psql -v ON_ERROR_STOP=1 -v pw="$DB_PASS" -v gw="$NET_GW" <<'SQL'
SELECT 'CREATE ROLE n8n LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE' WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='n8n') \gexec
ALTER ROLE n8n WITH PASSWORD :'pw';
SELECT 'CREATE DATABASE n8n OWNER n8n' WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname='n8n') \gexec
REVOKE ALL ON DATABASE n8n FROM PUBLIC;
ALTER SYSTEM SET listen_addresses = 'localhost, 172.30.0.1';
ALTER SYSTEM SET password_encryption = 'scram-sha-256';
ALTER SYSTEM SET log_connections = on;
ALTER SYSTEM SET shared_buffers = '512MB';
ALTER SYSTEM SET effective_cache_size = '1536MB';
ALTER SYSTEM SET work_mem = '16MB';
ALTER SYSTEM SET maintenance_work_mem = '128MB';
ALTER SYSTEM SET max_connections = 50;
SQL
systemctl restart postgresql
ss -ltn | grep ':5432 ' || { echo "postgres not listening"; exit 1; }

echo "== n8n files"
install -d -m 750 -o root -g root /etc/n8n
install -d -m 700 -o root -g root /etc/n8n/secrets
printf '%s' "$DB_PASS" > /etc/n8n/secrets/db_password
printf '%s' "$ENC_KEY" > /etc/n8n/secrets/encryption_key
chown 1000:1000 /etc/n8n/secrets/*; chmod 400 /etc/n8n/secrets/*      # readable by the container user only
install -d -m 700 -o 1000 -g 1000 /var/lib/n8n/data

cat > /etc/n8n/compose.env <<EOF
N8N_VERSION=${N8N_VERSION}
HOST_IP=${HOST_IP}
N8N_RUNNERS_AUTH_TOKEN=${RUNNER_TOKEN}
EOF
chmod 600 /etc/n8n/compose.env

cat > /etc/n8n/n8n.env <<EOF
# --- URLs: TLS ends at the ALB, one proxy hop (do not set N8N_HOST / N8N_PROTOCOL) ---
N8N_PORT=5678
N8N_EDITOR_BASE_URL=${N8N_PUBLIC_URL}
N8N_WEBHOOK_URL=${N8N_PUBLIC_URL}
N8N_PROXY_HOPS=1
N8N_PUSH_BACKEND=websocket

# --- database: host PostgreSQL via the docker bridge gateway; password via *_FILE secret ---
DB_TYPE=postgresdb
DB_POSTGRESDB_HOST=${NET_GW}
DB_POSTGRESDB_PORT=5432
DB_POSTGRESDB_DATABASE=n8n
DB_POSTGRESDB_USER=n8n
DB_POSTGRESDB_POOL_SIZE=4

# --- task runners: external sidecar ---
N8N_RUNNERS_MODE=external
N8N_RUNNERS_BROKER_LISTEN_ADDRESS=0.0.0.0

# --- security ---
N8N_ENFORCE_SETTINGS_FILE_PERMISSIONS=true
N8N_BLOCK_ENV_ACCESS_IN_NODE=true
N8N_SECURE_COOKIE=true
N8N_PUBLIC_API_DISABLED=true
N8N_COMMUNITY_PACKAGES_ENABLED=false
N8N_TEMPLATES_ENABLED=false
N8N_DIAGNOSTICS_ENABLED=false
N8N_VERSION_NOTIFICATIONS_ENABLED=true
N8N_PAYLOAD_SIZE_MAX=1
N8N_FORMDATA_FILE_SIZE_MAX=1

# --- executions ---
EXECUTIONS_TIMEOUT=300
EXECUTIONS_TIMEOUT_MAX=900
EXECUTIONS_DATA_PRUNE=true
EXECUTIONS_DATA_MAX_AGE=168
EXECUTIONS_DATA_PRUNE_MAX_COUNT=10000
EXECUTIONS_DATA_SAVE_MANUAL_EXECUTIONS=true

# --- runtime ---
GENERIC_TIMEZONE=${TZ_NAME}
TZ=${TZ_NAME}
N8N_LOG_LEVEL=info
EOF
chmod 600 /etc/n8n/n8n.env

cat > /etc/n8n/compose.yaml <<'EOF'
name: n8n

networks:
  n8n-net:
    external: true

secrets:
  db_password:
    file: /etc/n8n/secrets/db_password
  encryption_key:
    file: /etc/n8n/secrets/encryption_key

services:
  n8n:
    image: docker.n8n.io/n8nio/n8n:${N8N_VERSION}
    container_name: n8n
    restart: unless-stopped
    user: "1000:1000"
    read_only: true
    tmpfs:
      - /tmp:size=256m,mode=1777
      - /home/node/.cache:size=256m,uid=1000,gid=1000     # n8n writes UI assets here at startup
    cap_drop: [ALL]
    security_opt:
      - no-new-privileges:true
    pids_limit: 512
    deploy:
      resources:
        limits:
          cpus: "1.5"
          memory: 2g
    networks: [n8n-net]
    ports:
      - "${HOST_IP}:5678:5678"
    env_file: /etc/n8n/n8n.env
    environment:
      DB_POSTGRESDB_PASSWORD_FILE: /run/secrets/db_password
      N8N_ENCRYPTION_KEY_FILE: /run/secrets/encryption_key
      N8N_RUNNERS_AUTH_TOKEN: ${N8N_RUNNERS_AUTH_TOKEN}
    secrets: [db_password, encryption_key]
    volumes:
      - /var/lib/n8n/data:/home/node/.n8n
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://127.0.0.1:5678/healthz >/dev/null || exit 1"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 90s

  runners:
    image: docker.n8n.io/n8nio/runners:${N8N_VERSION}
    container_name: n8n-runners
    restart: unless-stopped
    read_only: true
    tmpfs:
      - /tmp:size=256m
    cap_drop: [ALL]
    security_opt:
      - no-new-privileges:true
    pids_limit: 256
    deploy:
      resources:
        limits:
          cpus: "1"
          memory: 1g
    networks: [n8n-net]
    environment:
      N8N_RUNNERS_TASK_BROKER_URI: http://n8n:5679
      N8N_RUNNERS_AUTH_TOKEN: ${N8N_RUNNERS_AUTH_TOKEN}
      N8N_RUNNERS_MAX_CONCURRENCY: "5"
    depends_on:
      n8n:
        condition: service_healthy
EOF
chmod 600 /etc/n8n/compose.yaml

cat > /etc/systemd/system/n8n.service <<'EOF'
[Unit]
Description=n8n (docker compose)
Requires=docker.service postgresql.service
After=docker.service postgresql.service network-online.target
Wants=network-online.target

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/etc/n8n
EnvironmentFile=/etc/n8n/compose.env
ExecStart=/usr/bin/docker compose -f /etc/n8n/compose.yaml up -d --remove-orphans
ExecStop=/usr/bin/docker compose -f /etc/n8n/compose.yaml down

[Install]
WantedBy=multi-user.target
EOF

echo "== pull images and record digests"
( cd /etc/n8n && set -a && . ./compose.env && set +a && docker compose pull --quiet )
docker image inspect --format '{{index .RepoDigests 0}}' "docker.n8n.io/n8nio/n8n:${N8N_VERSION}"     | tee -a /etc/n8n/image-digests.txt
docker image inspect --format '{{index .RepoDigests 0}}' "docker.n8n.io/n8nio/runners:${N8N_VERSION}" | tee -a /etc/n8n/image-digests.txt

echo "== start"
systemctl daemon-reload
systemctl enable --now n8n

echo "== backup job"
cat > /usr/local/sbin/n8n-backup.sh <<'EOF'
#!/bin/bash
set -euo pipefail
BUCKET=us-west-2.backup.thebridgeto.ai
STAMP=$(date -u +%Y-%m-%dT%H%M%SZ)
sudo -u postgres pg_dump -Fc n8n | aws s3 cp --region us-west-2 --only-show-errors - "s3://${BUCKET}/n8n/postgres/n8n-${STAMP}.dump"
tar -C /var/lib/n8n -czf - data --exclude='data/n8nEventLog*' --exclude='data/crash.journal' \
  | aws s3 cp --region us-west-2 --only-show-errors - "s3://${BUCKET}/n8n/data/n8n-${STAMP}.tar.gz"
EOF
chmod 700 /usr/local/sbin/n8n-backup.sh
cat > /etc/systemd/system/n8n-backup.service <<'EOF'
[Unit]
Description=Back up n8n database and data directory to S3
[Service]
Type=oneshot
ExecStart=/usr/local/sbin/n8n-backup.sh
EOF
cat > /etc/systemd/system/n8n-backup.timer <<'EOF'
[Unit]
Description=Nightly n8n backup
[Timer]
OnCalendar=*-*-* 09:00:00 UTC
Persistent=true
[Install]
WantedBy=timers.target
EOF
systemctl daemon-reload
systemctl enable --now n8n-backup.timer

echo "== local checks"
sleep 20
docker compose -f /etc/n8n/compose.yaml ps
curl -sf "http://${HOST_IP}:5678/healthz" && echo
```

If the runner container refuses to start under `read_only`, drop that one
line for the `runners` service and restart; the n8n container should keep it.

## 6. Verification

Run these before decommissioning anything.

1. **Target healthy.** `aws elbv2 describe-target-health --target-group-arn $TG_ARN` reports `healthy` within a minute or two.
2. **Encryption key came from SSM, not auto-generated.** On the box: `docker exec n8n sh -c 'grep -c encryptionKey /home/node/.n8n/config || true'` must print `0`. A `1` means n8n generated its own key and stored it in the config file; stop, fix the secret mount, wipe `/var/lib/n8n/data`, restart.
3. **Runner connected.** `docker logs n8n 2>&1 | grep -i runner` shows the runner registering. Then in the editor, run a workflow with a Code node returning a constant.
4. **Containers cannot reach instance credentials.** `docker exec n8n wget -qO- --timeout=2 http://169.254.169.254/latest/meta-data/ || echo BLOCKED` prints `BLOCKED`.
5. **PostgreSQL not on the instance IP.** `ss -ltn | grep 5432` lists only `127.0.0.1` and `172.30.0.1`.
6. **Admin lock.** From your IP, `curl -sI https://n8n.thebridgeto.ai/healthz` is 200 and the editor loads; create the owner account and enable two-factor authentication under Settings, Personal. From a phone on cellular, the same URL returns 403.
7. **Webhooks are public.** From the phone, `https://n8n.thebridgeto.ai/webhook/does-not-exist` returns n8n's own 404 JSON, proving rule 10 forwards.
8. **Backup works.** `systemctl start n8n-backup.service && aws s3 ls s3://us-west-2.backup.thebridgeto.ai/n8n/ --recursive` shows two fresh objects.
9. **Reboot survives.** `sudo reboot`, wait, and confirm the target returns to healthy without intervention (this exercises the docker → postgres → n8n ordering).

## 7. Operations

**Admin IP changed.**

```bash
RULE_20=$(aws elbv2 describe-rules --listener-arn $LISTENER_443 --query "Rules[?Priority=='20'].RuleArn" --output text)
aws elbv2 modify-rule --rule-arn $RULE_20 --conditions "[{\"Field\":\"host-header\",\"HostHeaderConfig\":{\"Values\":[\"$N8N_HOST\"]}},{\"Field\":\"source-ip\",\"SourceIpConfig\":{\"Values\":[\"$(curl -s https://checkip.amazonaws.com)/32\"]}}]"
```

Up to five CIDRs fit in that condition. Tailscale is already in use for
`coder.thebridgeto.ai`; a subnet router in the VPC would let rule 20 match
the VPC CIDR instead of a changing home IP.

**Upgrade n8n.** Both images move together. Read the release notes, run the
backup, then on the box:

```bash
sed -i 's/^N8N_VERSION=.*/N8N_VERSION=2.x.y/' /etc/n8n/compose.env && cd /etc/n8n && set -a && . ./compose.env && set +a && docker compose pull --quiet && systemctl restart n8n && docker logs -f n8n
```

Roll back by restoring the previous version string, pulling, restarting, and
restoring the database dump if a migration ran.

**Logs.** `docker logs -f n8n` and `docker logs -f n8n-runners`. PostgreSQL:
`journalctl -u postgresql`. WAF blocks: CloudWatch log group
`aws-waf-logs-prod-waf-website`. ALB 403s from rule 30 appear in ALB access
logs once those are enabled (section 8).

**Restore.** New instance via section 4.7 and the script in section 5, then
as postgres `pg_restore -d n8n --clean --if-exists n8n-<stamp>.dump`, untar
the data directory into `/var/lib/n8n/data`, `chown -R 1000:1000`, and restart
n8n. The encryption key comes from the same SSM parameter.

**Rotate the database password.** Put a new value in SSM, `ALTER ROLE n8n
WITH PASSWORD`, rewrite `/etc/n8n/secrets/db_password`, restart n8n.

**Long-running webhooks.** The ALB idle timeout is 60 s. Respond immediately
and process asynchronously rather than raising the timeout, which would also
apply to the website.

## 8. Suggestions and further hardening

In rough order of value:

1. **Enable ALB access logs** to an S3 bucket. Today there is no record of
   who hit the n8n host and got a 403, or of webhook traffic patterns.
2. **Add an n8n-specific WAF rule** to the `prod-waf-stack.yaml` template: a
   rate limit of, say, 60 requests per 5 minutes per IP scoped to the n8n host
   and `/webhook/`, plus a body size cap. The existing 2000-per-5-minutes rule
   is tuned for the website. The IP allowlist could also live here as an IP
   set, but the listener rule already does that job without touching the
   stack.
3. **Alarms.** An SNS topic with CloudWatch alarms on the target group's
   `UnHealthyHostCount`, the instance's `StatusCheckFailed`, and, with the
   CloudWatch agent, disk and memory. The WAF stack already has an alarm
   parameter waiting for a topic ARN.
4. **SSM session logging** to CloudWatch or S3 so every admin shell is
   recorded. One account-level Session Manager preference.
5. **Pin images by digest.** After the first pull, `/etc/n8n/image-digests.txt`
   holds the digests; switch `image:` to `n8nio/n8n@sha256:...` so a
   re-pushed tag cannot change what runs.
6. **Bot mitigation on the public form.** A honeypot field plus a Cloudflare
   Turnstile or hCaptcha token verified in the workflow before anything is
   stored or emailed. The WAF cannot tell a real submission from a script.
7. **Customer-managed KMS key** for the SSM parameters and the backup bucket,
   so key use is auditable and can be revoked independently of the account.
8. **Capture sections 4 and 5 in CloudFormation** as `prod-n8n-stack.yaml`
   beside `prod-waf-stack.yaml`, with the install script as user-data and the
   admin CIDR as a parameter. That is how the website is managed, and it
   makes rebuilds and the eventual RDS move reviewable diffs.
9. **RDS when you outgrow "for now".** A single-AZ db.t4g.micro (~$14/month)
   gives automated backups, point-in-time recovery, and takes PostgreSQL off
   the box. Change `DB_POSTGRESDB_HOST`, add the RDS security group rule, and
   drop the host packages.
10. **Rootless Docker or Podman.** The last step for container isolation:
    the daemon itself runs unprivileged. It complicates the container-to-host
    PostgreSQL path, which is why it is not in the base plan. Becomes simpler
    once PostgreSQL is on RDS.
11. **ECS Fargate** is the natural end state if you want no host at all:
    same ALB, WAF, listener rules and secrets, with RDS. Roughly $18/month
    more than this plan.

Deliberately left out: Docker user namespace remapping (complicates volume
ownership for little gain over a non-root container with all capabilities
dropped), and TLS between the container and host PostgreSQL (traffic never
leaves the host).

## 9. Follow-on: the static site form

When the S3/CloudFront site goes live, add the ALB as a second origin on its
distribution with origin domain `n8n.thebridgeto.ai`, origin path `/webhook`,
and a behavior for `/api/*` with caching disabled and the all-viewer origin
request policy. The form posts to `/api/subscribe` same-origin, CloudFront
forwards it to `/webhook/api/subscribe`, and rule 10 admits it. The ALB then
sees CloudFront's IP, so the per-IP rate limit for the form belongs on the
distribution's own WAF, not this one.

## Appendix A: keep the wizard-launched instance instead

If you would rather not relaunch:

1. An instance cannot change subnets in place, so it stays in the default
   VPC. Release the Elastic IP but keep a public IP for egress, because the
   default VPC has no NAT gateway. The security group is then the only
   network boundary.
2. Grow the root volume: `aws ec2 modify-volume --volume-id vol-07fb71d852ec9e76b --size 30`,
   then on the box `sudo growpart /dev/nvme0n1 1 && sudo xfs_growfs /`.
3. Attach the instance profile from 4.3:
   `aws ec2 associate-iam-instance-profile --instance-id i-05042d1f1a1fc8cad --iam-instance-profile Name=prod-n8n-app-profile`,
   and set `HttpPutResponseHopLimit=1` with `aws ec2 modify-instance-metadata-options`.
4. Create an ALB in two default-VPC subnets with its own security group, an
   ACM cert, the three listener rules from 4.8, and deploy a second copy of
   `prod-waf-stack.yaml` with `ClientName=n8n` against it. Replace the wizard
   security group rules with 5678 from the new ALB group only.
5. Run section 5 unchanged. Node 22 on the box is unused and can be removed.

Cost is roughly $16 for the ALB and $6 for the WAF per month on top of this
plan, and the box keeps a public IP.
