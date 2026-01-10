# 🔒 DevSecOps Pipeline Documentation

This document explains the security scanning pipeline implemented for the TrustMart Microservices application.

## 📋 Overview

The DevSecOps pipeline automatically runs security checks on every push and pull request:

| Tool | Type | What it Scans | When it Runs |
|------|------|---------------|--------------|
| **SonarQube** | SAST | Source code | Every push/PR |
| **OWASP Dependency-Check** | SCA | Maven/npm dependencies | Every push/PR |
| **Trivy** | Container Scanner | Docker images | After build |
| **npm audit** | SCA | Frontend dependencies | Every push/PR |

## 🚀 Pipeline Flow

```
┌─────────────┐    ┌─────────────────┐    ┌───────────────┐
│   Commit    │───>│   SonarQube     │───>│    Build      │
│    Code     │    │   Analysis      │    │  Application  │
└─────────────┘    └─────────────────┘    └───────────────┘
                           │                      │
                           ▼                      ▼
                   ┌───────────────┐      ┌───────────────┐
                   │    OWASP      │      │    Trivy      │
                   │ Dependency    │      │   Container   │
                   │    Check      │      │     Scan      │
                   └───────────────┘      └───────────────┘
                           │                      │
                           ▼                      ▼
                   ┌─────────────────────────────────────┐
                   │        Security Summary Report       │
                   └─────────────────────────────────────┘
                                    │
                                    ▼
                   ┌─────────────────────────────────────┐
                   │     Push to Registry (main only)    │
                   └─────────────────────────────────────┘
```

## ⚙️ Setup Instructions

### 1. GitHub Secrets Configuration

Go to your GitHub repository → Settings → Secrets and variables → Actions

Add the following secrets:

| Secret Name | Description | Required |
|-------------|-------------|----------|
| `SONAR_TOKEN` | SonarQube authentication token | Yes |
| `SONAR_HOST_URL` | SonarQube server URL (e.g., `https://sonarcloud.io`) | Yes |
| `DOCKERHUB_USERNAME` | Docker Hub username | For image push |
| `DOCKERHUB_TOKEN` | Docker Hub access token | For image push |

### 2. SonarQube Setup Options

#### Option A: Use SonarCloud (Recommended for open source)

1. Go to [SonarCloud.io](https://sonarcloud.io)
2. Sign in with GitHub
3. Import your repository
4. Copy the project token
5. Add to GitHub secrets as `SONAR_TOKEN`
6. Set `SONAR_HOST_URL` to `https://sonarcloud.io`

#### Option B: Run SonarQube Locally

```bash
# Start SonarQube locally
docker-compose -f docker-compose.security.yml up -d

# Wait for startup (takes 1-2 minutes)
# Access at: http://localhost:9000
# Default credentials: admin/admin

# Run analysis locally
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_LOCAL_TOKEN
```

### 3. Running Security Scans Locally

#### OWASP Dependency Check

```bash
# Run dependency check on all modules
mvn org.owasp:dependency-check-maven:aggregate

# View report
open target/dependency-check-report.html
```

#### Trivy Container Scan

```bash
# Install Trivy (Windows with Chocolatey)
choco install trivy

# Or download from: https://github.com/aquasecurity/trivy/releases

# Scan an image
trivy image trustmart/product-service:latest

# Scan with config file
trivy image --config trivy.yaml trustmart/product-service:latest

# Generate HTML report
trivy image --format template --template "@contrib/html.tpl" -o report.html trustmart/product-service:latest
```

#### npm Audit (Frontend)

```bash
cd trustmart-frontend
npm audit
npm audit --json > audit-report.json
```

## 📊 Understanding Reports

### SonarQube Report

- **Bugs**: Code that will break or behave unexpectedly
- **Vulnerabilities**: Security issues that could be exploited
- **Code Smells**: Maintainability issues
- **Security Hotspots**: Code that needs manual security review
- **Coverage**: Percentage of code covered by tests

### OWASP Dependency-Check Report

- **CVE ID**: Unique identifier for the vulnerability
- **Severity**: CRITICAL, HIGH, MEDIUM, LOW
- **CVSS Score**: Numerical severity (0-10)
- **Package**: The affected dependency
- **Fix**: Recommended version to upgrade to

### Trivy Report

- **Library**: Affected package name
- **Vulnerability**: CVE ID
- **Severity**: CRITICAL, HIGH, MEDIUM, LOW
- **Installed Version**: Current version in image
- **Fixed Version**: Version that fixes the issue

## 🔧 Configuration Files

| File | Purpose |
|------|---------|
| `.github/workflows/devsecops-pipeline.yml` | Main CI/CD pipeline |
| `sonar-project.properties` | SonarQube configuration |
| `owasp-suppressions.xml` | Suppress false positives for OWASP |
| `trivy.yaml` | Trivy scanner configuration |
| `.trivyignore` | Ignore specific CVEs in Trivy |
| `docker-compose.security.yml` | Local SonarQube setup |

## 🛠️ Handling Vulnerabilities

### Step 1: Review the Report

Check if the vulnerability:
- Is a false positive
- Applies to your use case
- Has a fix available

### Step 2: Fix or Suppress

**To fix a dependency vulnerability:**
```xml
<!-- Update version in pom.xml -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>vulnerable-lib</artifactId>
    <version>2.0.0</version> <!-- Fixed version -->
</dependency>
```

**To suppress a false positive (OWASP):**
```xml
<!-- Add to owasp-suppressions.xml -->
<suppress>
    <notes>Not applicable - we don't use the affected feature</notes>
    <cve>CVE-2021-XXXXX</cve>
</suppress>
```

**To suppress in Trivy:**
```
# Add to .trivyignore
CVE-2021-XXXXX
```

### Step 3: Document the Decision

Always document why a vulnerability is being suppressed or accepted.

## 📈 Quality Gates

The pipeline enforces these quality gates:

| Check | Threshold | Action if Failed |
|-------|-----------|------------------|
| SonarQube Quality Gate | Configurable | Fails pipeline |
| OWASP CVSS Score | ≥ 7 (High) | Warning (configurable) |
| Trivy Critical/High | Any found | Reports only |

## 🔄 Continuous Improvement

1. **Review suppressions monthly** - Remove when fixes are available
2. **Update base images regularly** - Keep containers patched
3. **Monitor dependency updates** - Use Dependabot or Renovate
4. **Review Security Hotspots** - Manual review in SonarQube

## 📚 Resources

- [SonarQube Documentation](https://docs.sonarqube.org/)
- [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/)
- [Trivy Documentation](https://aquasecurity.github.io/trivy/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CVE Database](https://cve.mitre.org/)

