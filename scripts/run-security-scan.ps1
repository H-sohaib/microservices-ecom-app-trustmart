# ==============================================================================
# Local Security Scan Script (PowerShell)
# ==============================================================================
# This script runs all security scans locally without needing GitHub Actions
#
# Usage:
#   .\run-security-scan.ps1 [-Scan <type>]
#
# Examples:
#   .\run-security-scan.ps1                    # Run all scans
#   .\run-security-scan.ps1 -Scan owasp        # Run OWASP only
#   .\run-security-scan.ps1 -Scan trivy        # Run Trivy only
#   .\run-security-scan.ps1 -Scan npm          # Run npm audit only
# ==============================================================================

param(
    [ValidateSet("all", "owasp", "trivy", "sonar", "npm")]
    [string]$Scan = "all"
)

$ErrorActionPreference = "Continue"

# Create reports directory
New-Item -ItemType Directory -Force -Path "security-reports" | Out-Null
New-Item -ItemType Directory -Force -Path "security-reports\trivy" | Out-Null

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Run-OWASP {
    Write-Header "Running OWASP Dependency Check"

    try {
        & mvn org.owasp:dependency-check-maven:aggregate `
            "-DfailBuildOnCVSS=7" `
            "-Dformats=HTML,JSON" `
            "-DprettyPrint=true" `
            --batch-mode

        if ($LASTEXITCODE -eq 0) {
            Write-Success "OWASP scan completed successfully"
        } else {
            Write-Warning "OWASP scan found vulnerabilities (check report)"
        }
    } catch {
        Write-Warning "OWASP scan encountered issues: $_"
    }

    Write-Host "Report: target\dependency-check-report.html" -ForegroundColor Gray
}

function Run-Trivy {
    Write-Header "Running Trivy Container Scans"

    # Check if Trivy is installed
    $trivyPath = Get-Command trivy -ErrorAction SilentlyContinue
    if (-not $trivyPath) {
        Write-Error "Trivy is not installed"
        Write-Host "Install with: choco install trivy" -ForegroundColor Gray
        Write-Host "Or download from: https://github.com/aquasecurity/trivy/releases" -ForegroundColor Gray
        return
    }

    $services = @(
        "discovery-service",
        "gateway-service",
        "product-service",
        "command-service",
        "frontend"
    )

    foreach ($service in $services) {
        Write-Host "`nScanning $service..." -ForegroundColor Yellow

        $image = "microservices-ecom-app-${service}:latest"

        # Check if image exists
        $imageExists = docker image inspect $image 2>$null
        if ($LASTEXITCODE -eq 0) {
            # Run Trivy scan
            & trivy image --severity HIGH,CRITICAL $image `
                --format table `
                --output "security-reports\trivy\${service}-report.txt"

            & trivy image --severity HIGH,CRITICAL $image `
                --format json `
                --output "security-reports\trivy\${service}-report.json"

            Write-Success "$service scanned"
        } else {
            Write-Warning "Image $image not found, skipping"
        }
    }

    Write-Host "`nReports saved to: security-reports\trivy\" -ForegroundColor Gray
}

function Run-SonarQube {
    Write-Header "Running SonarQube Analysis"

    $sonarUrl = if ($env:SONAR_HOST_URL) { $env:SONAR_HOST_URL } else { "http://localhost:9000" }

    Write-Host "SonarQube URL: $sonarUrl" -ForegroundColor Gray

    if (-not $env:SONAR_TOKEN) {
        Write-Error "SONAR_TOKEN environment variable not set"
        Write-Host 'Set it with: $env:SONAR_TOKEN = "your_token"' -ForegroundColor Gray
        return
    }

    # Build first
    Write-Host "Building project..."
    & mvn clean verify -DskipTests -q

    # Run SonarQube
    & mvn sonar:sonar `
        "-Dsonar.host.url=$sonarUrl" `
        "-Dsonar.token=$env:SONAR_TOKEN"

    if ($LASTEXITCODE -eq 0) {
        Write-Success "SonarQube analysis completed"
        Write-Host "View results at: $sonarUrl/dashboard?id=trustmart-microservices" -ForegroundColor Gray
    } else {
        Write-Error "SonarQube analysis failed"
    }
}

function Run-NpmAudit {
    Write-Header "Running npm Audit (Frontend)"

    Push-Location trustmart-frontend

    try {
        & npm audit --json 2>$null | Out-File -FilePath "..\security-reports\npm-audit-report.json"
        & npm audit

        if ($LASTEXITCODE -eq 0) {
            Write-Success "No vulnerabilities found"
        } else {
            Write-Warning "Vulnerabilities found (check report)"
        }
    } catch {
        Write-Warning "npm audit encountered issues"
    }

    Pop-Location

    Write-Host "Report: security-reports\npm-audit-report.json" -ForegroundColor Gray
}

# Main execution
switch ($Scan) {
    "owasp" { Run-OWASP }
    "trivy" { Run-Trivy }
    "sonar" { Run-SonarQube }
    "npm" { Run-NpmAudit }
    "all" {
        Run-OWASP
        Run-NpmAudit
        Run-Trivy

        Write-Header "Security Scan Summary"
        Write-Host "All scans completed. Check the following reports:"
        Write-Host "  - OWASP: target\dependency-check-report.html" -ForegroundColor Gray
        Write-Host "  - npm:   security-reports\npm-audit-report.json" -ForegroundColor Gray
        Write-Host "  - Trivy: security-reports\trivy\" -ForegroundColor Gray
    }
}

