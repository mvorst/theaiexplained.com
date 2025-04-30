#!/bin/bash
# deploy.sh - Script to update Spring Boot app from S3 and restart the service

# Variables (modify as needed)
S3_WAR="s3://us-west-1.ci.theaiexplained.com/build/TheAIExplained_com-0.0.1.war"
DEST_WAR="/opt/app/webapp.war"
S3_DOCS="s3://us-west-1.ci.theaiexplained.com/build/AIExplained_Docs-CI-0.0.1.zip"
DOCS_DEST="/var/www"
SERVICE_NAME="webapp.service"  # Change to your service name

echo "Clearing logs"
journalctl --rotate
journalctl --vacuum-time=1s

# Log the start time
echo "Deployment started at: $(date)"

# Download the WAR from S3 to the destination
echo "Downloading ${S3_WAR} to ${DEST_WAR}..."
aws s3 cp "${S3_WAR}" "${DEST_WAR}"
if [ $? -ne 0 ]; then
    echo "Error: Failed to download WAR from S3."
    exit 1
fi

# Download and unzip the documentation
echo "Downloading and extracting ${S3_DOCS} to ${DOCS_DEST}..."
aws s3 cp "${S3_DOCS}" "/tmp/docs.zip"
if [ $? -ne 0 ]; then
    echo "Error: Failed to download documentation from S3."
    exit 1
fi

# Make sure the destination directory exists
sudo mkdir -p "${DOCS_DEST}"

# Extract the docs to the destination
sudo unzip -o "/tmp/docs.zip" -d "${DOCS_DEST}"
if [ $? -ne 0 ]; then
    echo "Error: Failed to extract documentation."
    exit 1
fi

# Clean up the temporary zip file
rm -f "/tmp/docs.zip"

# (Optional) Adjust file permissions if required
# For example, ensure the file is owned by the proper user and has execute permissions.
# Change "appuser:appuser" to the appropriate user:group.
sudo chown ec2-user:ec2-user "${DEST_WAR}"
sudo chmod 755 "${DEST_WAR}"

# Set proper permissions for the web content
sudo chown -R www-data:www-data "${DOCS_DEST}"
sudo chmod -R 755 "${DOCS_DEST}"

# Restart the Spring Boot application service
echo "Restarting the service ${SERVICE_NAME}..."
sudo systemctl restart "${SERVICE_NAME}"
if [ $? -ne 0 ]; then
    echo "Error: Failed to restart service ${SERVICE_NAME}."
    exit 1
fi

echo "Deployment complete at: $(date)"