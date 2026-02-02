# Security Policy

## Supported Versions

Currently supported versions for security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.5.x   | :white_check_mark: |
| < 1.5   | :x:                |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please follow these steps:

### DO NOT create a public GitHub issue

Instead:

1. **Email**: Send details to [your-email@example.com] with subject "Git Store Security Vulnerability"

2. **Include**:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

3. **Response Time**:
   - Initial response: Within 48 hours
   - Status update: Within 7 days
   - Fix timeline: Depends on severity

## Security Best Practices

### For Users:
- Download APKs only from official GitHub releases
- Verify APK signatures match our certificate fingerprint
- Keep the app updated to the latest version
- Review permissions before installing

### For Developers:
- Never commit sensitive data (API keys, tokens)
- Use environment variables for secrets
- Follow secure coding practices
- Keep dependencies updated

## Disclosure Policy

- Security fixes are released as soon as possible
- We credit researchers who report vulnerabilities (if desired)
- Details are disclosed after a fix is available

## Security Features

Git Store implements:
- HTTPS-only connections
- Certificate pinning for API calls
- Secure token storage using Android DataStore
- No tracking or analytics
- Open source code for transparency

---

Thank you for helping keep Git Store secure! 🔒
