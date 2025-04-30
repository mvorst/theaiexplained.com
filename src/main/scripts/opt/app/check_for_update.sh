#!/bin/bash
# Filename: check_for_update.sh
# Description: Checks DynamoDB for CI build numbers and restarts a service if the deployed build is outdated.
# This script uses a lock file to ensure that multiple instances don't run concurrently.
# Ensure this script is executable: chmod +x check_for_update.sh

cd /opt/app/

# Configuration
TABLE_NAME="system_properties"          # Replace with your DynamoDB table name
REGION="us-west-1"                      # Replace with your AWS region

# Lock file configuration
LOCKFILE="/tmp/check_for_update.lock"

# Check if lock file exists
if [ -e "$LOCKFILE" ]; then
  echo "Script is already running. Exiting."
  exit 1
fi

# Create lock file with current PID and ensure its removal on script exit.
echo $$ > "$LOCKFILE"
trap "rm -f '$LOCKFILE'" EXIT

# Retrieve the expected build number (stored as a string) from DynamoDB.
EXPECTED=$(aws dynamodb get-item \
  --table-name "$TABLE_NAME" \
  --key '{"propertyType": {"S": "CI_BUILD_NUMBER_EXPECTED"}}' \
  --query 'Item.value.S' \
  --output text \
  --region "$REGION")

# Retrieve the deployed build number (stored as a string) from DynamoDB.
DEPLOYED=$(aws dynamodb get-item \
  --table-name "$TABLE_NAME" \
  --key '{"propertyType": {"S": "CI_BUILD_NUMBER_DEPLOYED"}}' \
  --query 'Item.value.S' \
  --output text \
  --region "$REGION")

# Check that we received valid values.
if [ -z "$EXPECTED" ] || [ -z "$DEPLOYED" ]; then
  echo "Error: Could not retrieve build numbers from DynamoDB." >&2
  exit 1
fi

echo "Expected Build Number: $EXPECTED"
echo "Deployed Build Number: $DEPLOYED"

# Convert the values to integers and compare.
if (( DEPLOYED < EXPECTED )); then
  echo "Deployed build number ($DEPLOYED) is less than expected ($EXPECTED). Restarting service..."
  # Restart the service (this example uses systemctl; adjust if needed)
  ./deploy.sh

  # Optionally, log the result
  if [ $? -eq 0 ]; then
    echo "$(date): Successfully deployed the update" >> /var/log/check_for_update.log
  else
    echo "$(date): Failed to deploy the update" >> /var/log/check_for_update.log
  fi
else
  echo "Deployed build number ($DEPLOYED) is up-to-date. No action required."
fi
