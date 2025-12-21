# HTTPD/AJP Configuration Guide (Local Development)

This document describes how to configure Apache HTTPD for local development to proxy requests to the Spring Boot application using AJP (Apache JServ Protocol).

## Overview

The architecture uses Apache HTTPD as a reverse proxy:
- **Static files** (CSS, JS, images) are served directly by Apache
- **Dynamic requests** are proxied to Tomcat via AJP
- **dev.theBridgeToAI.com** redirects to **dev.theBridgeTo.ai** (canonical dev domain)

## Prerequisites

### Application Configuration

Ensure these properties are set in `application.properties`:

```properties
server.tomcat.ajp.enabled=true
server.tomcat.ajp.port=8029
server.tomcat.ajp.secret=your-secret-here
server.tomcat.ajp.protocol=AJP/1.3
```

**Important**: Replace `your-secret-here` with a strong, unique secret. This secret must match in both the application and Apache configuration.

---

## MacOS Setup (Development)

Base configuration directory: `/etc/apache2/`

### 1. Enable Required Modules

Edit `/etc/apache2/httpd.conf`:

```apache
# Uncomment these lines
LoadModule proxy_module libexec/apache2/mod_proxy.so
LoadModule proxy_ajp_module libexec/apache2/mod_proxy_ajp.so
LoadModule rewrite_module libexec/apache2/mod_rewrite.so
LoadModule vhost_alias_module libexec/apache2/mod_vhost_alias.so

# Add at the end of the file
Include /etc/apache2/extra/httpd-vhosts.conf
```

### 2. Configure Virtual Hosts

Edit `/etc/apache2/extra/httpd-vhosts.conf`:

```apache
# Redirect dev.theBridgeToAI.com to dev.theBridgeTo.ai
<VirtualHost *:80>
    ServerName dev.theBridgeToAI.com

    RewriteEngine On
    RewriteRule ^(.*)$ http://dev.theBridgeTo.ai$1 [R=301,L]
</VirtualHost>

# Main dev site configuration
<VirtualHost *:80>
    ServerName dev.theBridgeTo.ai

    DocumentRoot "/Users/matthewvorst/IdeaProjects/TheBridgeTo.ai/src/main/docs"

    # Logging
    ErrorLog "/var/log/apache2/thebridgetoai-error.log"
    CustomLog "/var/log/apache2/thebridgetoai-access.log" combined

    # Static files directory
    <Directory "/Users/matthewvorst/IdeaProjects/TheBridgeTo.ai/src/main/docs">
        Options Indexes FollowSymLinks
        AllowOverride None
        Require all granted
    </Directory>

    # Proxy only specific patterns to Tomcat via AJP
    # Note: No trailing slash on ajp:// URL to avoid double-slash in path
    ProxyPassMatch "^/rest/.*$" "ajp://localhost:8029" secret=your-secret-here
    ProxyPassReverse "/rest/" "ajp://localhost:8029/"

    ProxyPassMatch "^/.*\.jsp$" "ajp://localhost:8029" secret=your-secret-here
    ProxyPassReverse "/" "ajp://localhost:8029/"

    ProxyPassMatch "^/.*\.action$" "ajp://localhost:8029" secret=your-secret-here

    # Everything else is served by Apache from DocumentRoot
</VirtualHost>
```

### 3. Start/Restart Apache

```bash
# Start Apache
sudo apachectl start

# Or restart if already running
sudo apachectl restart

# Check configuration syntax
apachectl configtest
```

### 4. Add Local DNS Entries

Edit `/etc/hosts`:

```
127.0.0.1   dev.theBridgeToAI.com
127.0.0.1   dev.theBridgeTo.ai
```

---

## Security Considerations

### AJP Secret

The AJP secret prevents unauthorized connections to your Tomcat AJP connector. Always:
- Use a strong, random secret (at least 32 characters)
- Never commit the secret to version control
- Use different secrets for each environment

Generate a secure secret:
```bash
openssl rand -base64 32
```

---

## Troubleshooting

### Check Apache Error Logs

```bash
sudo tail -f /var/log/apache2/thebridgetoai-error.log
```

### Verify AJP Connection

```bash
# Check if Tomcat AJP port is listening
lsof -i :8029
```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| 503 Service Unavailable | Tomcat not running or AJP not enabled | Start Tomcat and verify `server.tomcat.ajp.enabled=true` |
| 403 Forbidden | AJP secret mismatch | Ensure secret matches in both configs |
| Connection refused | Wrong port or firewall blocking | Check port 8029 is open locally |
| Static files return 404 | Incorrect DocumentRoot | Verify directory exists and has correct permissions |

### Test Connectivity

```bash
curl -v http://dev.theBridgeTo.ai/
```

---

## Directory Structure

Static files are served from the project's docs directory:

```
/Users/matthewvorst/IdeaProjects/TheBridgeTo.ai/src/main/docs/
├── static/
├── images/
├── css/
├── js/
├── fonts/
├── favicon.ico
└── robots.txt
```

---

## Related Files

- **Application AJP Config**: `src/main/java/com/thebridgetoai/website/config/TomcatConfig.java`
- **Application Properties**: `src/main/resources/application.properties`
