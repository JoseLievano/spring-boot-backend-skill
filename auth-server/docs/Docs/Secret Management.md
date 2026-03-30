## Table of Contents
1. [Overview](#overview)
2. [File Signature Secret](#file-signature-secret)
3. [Secret Generation](#secret-generation)
4. [Environment Configuration](#environment-configuration)
5. [Deployment Checklist](#deployment-checklist)
6. [Secret Rotation](#secret-rotation)
7. [Security Best Practices](#security-best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Overview

WP Manager uses environment-based secret management to protect cryptographic operations and ensure application security. This guide covers how to generate, configure, and manage secrets for production deployment.

### Why Environment-Based Secrets?

**12-Factor App Principles**:
- Secrets stored in environment variables, not source code
- Different secrets for each environment (development, staging, production)
- Secrets can be rotated without code changes
- No secrets committed to version control

**Security Benefits**:
- Source code can be public without exposing secrets
- Compromised repository doesn't compromise production
- Secrets managed by operations team, not developers
- Audit trail for secret access and changes

---

## File Signature Secret

### Purpose

The `FILE_SIGNATURE_SECRET` is used by [[FileSigner-java]] to generate HMAC-SHA256 signatures for uploaded files. These signatures:
- Link physical files to database records
- Prevent signature forgery and file tampering
- Enable secure file tracking across storage providers
- Protect file integrity throughout the system

### Requirements

**Minimum Length**: 32 characters (longer is better)
**Character Set**: Any printable ASCII characters
**Uniqueness**: Different secret per environment
**Randomness**: Must be cryptographically random (not dictionary words)

### Impact

**If Compromised**:
- Attackers could forge file signatures
- Malicious files could be uploaded with valid signatures
- File integrity verification system compromised
- Immediate secret rotation required

**If Misconfigured**:
- Application fails to start (fail-fast security)
- Clear error message indicates configuration issue
- No partial security degradation

---

## Secret Generation

### Method 1: OpenSSL (Linux/Mac)

**Command**:
```bash
openssl rand -base64 32
```

**Example Output**:
```
aBcD1234efGH5678ijKL9012mnOP3456qrST7890uvWX1234
```

**Advantages**:
- Built into most Unix-like systems
- Cryptographically secure random generator
- No additional dependencies

### Method 2: Python (Any Platform)

**Command**:
```bash
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

**Example Output**:
```
Xy9Z_aBc-1De2Fg3Hi4Jk5Lm6No7Pq8Rs9Tu0Vw1Xy2Za3Bc
```

**Advantages**:
- Works on Windows, Linux, Mac
- Python's `secrets` module is cryptographically secure
- URL-safe output (useful for various scenarios)

### Method 3: Online Generator (Use with Caution)

**Recommended Tools**:
- 1Password secret generator
- Bitwarden password generator
- LastPass password generator

**⚠️ Warning**: Only use trusted, reputable password managers. Never use random websites for production secrets.

### Method 4: Manual Generation (Not Recommended)

**If you must create manually**:
- Minimum 32 characters
- Mix uppercase, lowercase, numbers, symbols
- Avoid dictionary words, patterns, or personal information
- Use a password manager to generate and store

**Example**:
```
K7mN#p2Q$r9T&v4W*x1Y+z6A!c3E@h8J
```

---

## Environment Configuration

### Development Environment

**Option 1: .env File (For Local Development)**
```bash
# Create .env file in project root
FILE_SIGNATURE_SECRET=dev-secret-for-local-testing-minimum-32-characters
```

**Option 2: Shell Export**
```bash
export FILE_SIGNATURE_SECRET="dev-secret-for-local-testing-minimum-32-characters"
```

**Option 3: IDE Configuration**
- IntelliJ IDEA: Run Configurations → Environment Variables
- Eclipse: Run Configurations → Environment tab
- VS Code: launch.json → env property

### Staging Environment

**Docker**:
```bash
docker run -e FILE_SIGNATURE_SECRET="staging-secret-here" wpmanager:latest
```

**Docker Compose**:
```yaml
version: '3.8'
services:
  wpmanager:
    image: wpmanager:latest
    environment:
      - FILE_SIGNATURE_SECRET=${FILE_SIGNATURE_SECRET}
```

**Kubernetes**:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: wpmanager-secrets
type: Opaque
stringData:
  file-signature-secret: "staging-secret-here"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wpmanager
spec:
  template:
    spec:
      containers:
      - name: wpmanager
        env:
        - name: FILE_SIGNATURE_SECRET
          valueFrom:
            secretKeyRef:
              name: wpmanager-secrets
              key: file-signature-secret
```

### Production Environment

**Linux Systemd Service**:
```ini
# /etc/systemd/system/wpmanager.service
[Unit]
Description=WP Manager Application

[Service]
Environment="FILE_SIGNATURE_SECRET=prod-secret-here"
ExecStart=/opt/wpmanager/bin/wpmanager
User=wpmanager

[Install]
WantedBy=multi-user.target
```

**AWS Elastic Beanstalk**:
```bash
# Configure environment variable via AWS Console or CLI
aws elasticbeanstalk update-environment \
  --environment-name wpmanager-prod \
  --option-settings \
    Namespace=aws:elasticbeanstalk:application:environment,\
    OptionName=FILE_SIGNATURE_SECRET,\
    Value=prod-secret-here
```

**Heroku**:
```bash
heroku config:set FILE_SIGNATURE_SECRET="prod-secret-here" --app wpmanager-prod
```

**AWS ECS Task Definition**:
```json
{
  "containerDefinitions": [
    {
      "name": "wpmanager",
      "environment": [
        {
          "name": "FILE_SIGNATURE_SECRET",
          "value": "prod-secret-here"
        }
      ]
    }
  ]
}
```

---

## Deployment Checklist

### Pre-Deployment

- [ ] **Generate Secret**: Use secure random generation method
- [ ] **Verify Length**: Minimum 32 characters
- [ ] **Document Location**: Record where secret is stored (password manager, vault)
- [ ] **Backup Secret**: Store in secure backup location
- [ ] **Access Control**: Limit who can view/modify production secrets

### During Deployment

- [ ] **Set Environment Variable**: Configure FILE_SIGNATURE_SECRET
- [ ] **Test Configuration**: Run application startup validation
- [ ] **Verify Logs**: Check for configuration warnings
- [ ] **Test Upload**: Perform test file upload
- [ ] **Verify Signatures**: Confirm signatures are generated correctly

### Post-Deployment

- [ ] **Remove from Shell History**: Clear sensitive commands from history
- [ ] **Audit Access**: Log who configured the secret
- [ ] **Monitor Errors**: Watch for signature-related errors
- [ ] **Document Version**: Record which secret version is in use

### Deployment Verification

**Check Application Logs**:
```bash
# Look for successful startup without errors
grep "file.signature.secret" /var/log/wpmanager.log

# Should NOT see:
# ERROR: FILE_SIGNATURE_SECRET is not properly configured!
```

**Test Upload Functionality**:
```bash
curl -X POST http://localhost:8080/plugin/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test-plugin.zip" \
  -F "version=1.0.0" \
  -F "plugin=1"
```

**Expected Response**: HTTP 200 OK with DownloadableDTO

---

## Secret Rotation

### When to Rotate

**Mandatory Rotation**:
- Secret potentially compromised (leaked in logs, exposed in code)
- Team member with secret access leaves company
- Compliance requirements (e.g., quarterly rotation)
- Security breach or incident

**Recommended Rotation**:
- Every 90 days (compliance best practice)
- After major security updates
- When changing deployment infrastructure

### Rotation Process

**Phase 1: Generate New Secret**
```bash
# Generate new secret
NEW_SECRET=$(openssl rand -base64 32)
echo "New secret: $NEW_SECRET"
```

**Phase 2: Update Configuration**
```bash
# Update environment variable in deployment platform
# Example for Kubernetes:
kubectl create secret generic wpmanager-secrets \
  --from-literal=file-signature-secret="$NEW_SECRET" \
  --dry-run=client -o yaml | kubectl apply -f -
```

**Phase 3: Rolling Restart**
```bash
# Restart application with new secret
# Example for Kubernetes:
kubectl rollout restart deployment/wpmanager

# Example for Systemd:
sudo systemctl restart wpmanager
```

**Phase 4: Verification**
```bash
# Verify application started successfully
kubectl logs deployment/wpmanager | grep -i "signature"

# Test upload functionality
curl -X POST http://localhost:8080/plugin/upload [...]
```

**Phase 5: Cleanup**
```bash
# Securely delete old secret from password manager
# Update documentation with rotation date
# Clear shell history of sensitive commands
history -c
```

### Backward Compatibility Note

**Important**: Rotating the secret does NOT invalidate existing file signatures in the database. Old signatures continue to function as unique identifiers. The security improvement comes from:
1. New signatures cannot be forged without the new secret
2. Attackers cannot generate new malicious signatures
3. Existing files are tracked by their existing signatures

---

## Security Best Practices

### Secret Storage

**Do**:
- ✅ Use enterprise password managers (1Password, LastPass, Bitwarden)
- ✅ Use cloud secret managers (AWS Secrets Manager, Azure Key Vault, GCP Secret Manager)
- ✅ Use environment variables for runtime configuration
- ✅ Encrypt secrets at rest
- ✅ Audit secret access

**Don't**:
- ❌ Commit secrets to version control (.env files in .git)
- ❌ Store secrets in application code
- ❌ Share secrets via email or chat
- ❌ Store secrets in plain text files
- ❌ Use same secret across all environments

### Access Control

**Principle of Least Privilege**:
- Only operations team can view production secrets
- Developers use development secrets only
- Separate secrets per environment
- Rotate secrets when team members leave
- Audit trail for all secret access

### Monitoring and Alerts

**Set up alerts for**:
- Failed signature validation attempts
- Application startup failures related to secrets
- Unusual file upload patterns
- Secret rotation events

---

## Troubleshooting

### Error: "FILE_SIGNATURE_SECRET is not properly configured!"

**Cause**: Secret is not set, empty, or using default placeholder

**Solution**:
```bash
# Verify environment variable is set
echo $FILE_SIGNATURE_SECRET

# Set the variable
export FILE_SIGNATURE_SECRET="your-secure-secret-here"

# Restart application
```

### Warning: "file.signature.secret should be at least 32 characters"

**Cause**: Secret is too short

**Solution**:
```bash
# Generate longer secret
openssl rand -base64 48

# Update environment variable with new secret
```

### Error: "Error generating file signature"

**Cause**: Cryptographic operation failed

**Possible Solutions**:
1. Verify Java Cryptography Extension (JCE) is installed
2. Check for special characters in secret causing parsing issues
3. Review application logs for detailed error message
4. Verify secret is properly escaped in shell/config

### Application Won't Start After Secret Change

**Cause**: Invalid secret format or configuration issue

**Debug Steps**:
```bash
# Check environment variable is actually set
env | grep FILE_SIGNATURE_SECRET

# Check application.properties
cat src/main/resources/application.properties | grep file.signature.secret

# Check application logs
tail -f /var/log/wpmanager.log | grep -i secret

# Test with simple secret first
export FILE_SIGNATURE_SECRET="test-secret-minimum-32-characters-long"
```

### Signatures Not Matching After Rotation

**This is expected behavior**:
- Old signatures remain in database and are still valid
- New uploads create new signatures with new secret
- No re-signing of existing files needed
- System works with mixed old/new signatures

**If problematic**:
- Only rotate secret during maintenance window
- Document rotation for audit trail
- Monitor for any unexpected signature validation failures

---

## Related Documentation

- [[FileSigner-java]] - File signature generation implementation
- [[File Upload Process]] - Complete upload process with signature generation
- `src/main/resources/application.properties` - Configuration file with secret property

---

## Summary

Proper secret management is critical for WP Manager's security:

1. **Generate Strong Secrets**: Use cryptographic random generation (minimum 32 characters)
2. **Protect Secrets**: Store in environment variables, not source code
3. **Rotate Regularly**: Follow 90-day rotation schedule or as needed
4. **Control Access**: Limit who can view/modify production secrets
5. **Monitor Usage**: Set up alerts for signature-related errors

Following these guidelines ensures that file signatures remain cryptographically secure and cannot be forged by attackers, protecting the integrity of the entire file upload and verification system.
