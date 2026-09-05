#!/usr/bin/env bash
# Creates or updates the v2.thebridgeto.ai stack (S3 + CloudFront + ACM + Route53).
#
#   static-site/infra/deploy-infra.sh
#
# Environment overrides: AWS_PROFILE, STACK_NAME, SITE_HOSTNAME, HOSTED_ZONE_ID.
set -euo pipefail
cd "$(dirname "$0")"

PROFILE="${AWS_PROFILE:-claude_prod_thebridgeto_ai}"
REGION=us-east-1
STACK="${STACK_NAME:-thebridgetoai-v2-website}"
HOSTNAME_PARAM="${SITE_HOSTNAME:-v2.thebridgeto.ai}"
ZONE="${HOSTED_ZONE_ID:-Z00041362HB0V7RN0S0GA}"

echo "Deploying $STACK for $HOSTNAME_PARAM (zone $ZONE) with profile $PROFILE ..."
aws cloudformation deploy \
  --profile "$PROFILE" --region "$REGION" \
  --stack-name "$STACK" \
  --template-file website.yaml \
  --parameter-overrides "SiteHostname=$HOSTNAME_PARAM" "HostedZoneId=$ZONE" \
  --tags Project=TheBridgeToAI Environment=prod ManagedBy=CloudFormation \
  --no-fail-on-empty-changeset

aws cloudformation describe-stacks \
  --profile "$PROFILE" --region "$REGION" --stack-name "$STACK" \
  --query 'Stacks[0].Outputs[].[OutputKey,OutputValue]' --output table
