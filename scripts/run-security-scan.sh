#!/bin/bash
# ==============================================================================
# Local Security Scan Script
# ==============================================================================
# This script runs all security scans locally without needing GitHub Actions
#
# Usage:
#   ./run-security-scan.sh [options]
#
# Options:
#   --owasp     Run OWASP Dependency Check only
#   --trivy     Run Trivy container scan only
#   --sonar     Run SonarQube analysis only (requires running server)
#   --all       Run all scans (default)
#   --help      Show this help message
# ==============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored message
print_header() {
    echo -e "\n${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Run OWASP Dependency Check
run_owasp() {
    print_header "Running OWASP Dependency Check"

    if mvn org.owasp:dependency-check-maven:aggregate \
        -DfailBuildOnCVSS=7 \
        -Dformats=HTML,JSON \
        -DprettyPrint=true; then
        print_success "OWASP scan completed"
        echo "Report: target/dependency-check-report.html"
    else
        print_warning "OWASP scan found vulnerabilities (check report)"
    fi
}

# Run Trivy container scan
run_trivy() {
    print_header "Running Trivy Container Scans"

    if ! command_exists trivy; then
        print_error "Trivy is not installed"
        echo "Install with: choco install trivy (Windows)"
        echo "Or download from: https://github.com/aquasecurity/trivy/releases"
        return 1
    fi

    # Create reports directory
    mkdir -p security-reports/trivy

    # Scan each service image
    services=("discovery-service" "gateway-service" "product-service" "command-service" "frontend")

    for service in "${services[@]}"; do
        echo -e "\n${YELLOW}Scanning $service...${NC}"

        image="microservices-ecom-app-${service}:latest"

        if docker image inspect "$image" >/dev/null 2>&1; then
            trivy image --severity HIGH,CRITICAL "$image" \
                --format table \
                --output "security-reports/trivy/${service}-report.txt" || true

            trivy image --severity HIGH,CRITICAL "$image" \
                --format json \
                --output "security-reports/trivy/${service}-report.json" || true

            print_success "$service scanned"
        else
            print_warning "Image $image not found, skipping"
        fi
    done

    echo -e "\nReports saved to: security-reports/trivy/"
}

# Run SonarQube analysis
run_sonar() {
    print_header "Running SonarQube Analysis"

    SONAR_URL=${SONAR_HOST_URL:-"http://localhost:9000"}

    echo "SonarQube URL: $SONAR_URL"

    if [ -z "$SONAR_TOKEN" ]; then
        print_error "SONAR_TOKEN environment variable not set"
        echo "Set it with: export SONAR_TOKEN=your_token"
        return 1
    fi

    # Build first
    echo "Building project..."
    mvn clean verify -DskipTests -q

    # Run SonarQube
    if mvn sonar:sonar \
        -Dsonar.host.url="$SONAR_URL" \
        -Dsonar.token="$SONAR_TOKEN"; then
        print_success "SonarQube analysis completed"
        echo "View results at: $SONAR_URL/dashboard?id=trustmart-microservices"
    else
        print_error "SonarQube analysis failed"
        return 1
    fi
}

# Run npm audit for frontend
run_npm_audit() {
    print_header "Running npm Audit (Frontend)"

    cd trustmart-frontend

    if npm audit --json > ../security-reports/npm-audit-report.json 2>/dev/null; then
        print_success "No vulnerabilities found"
    else
        print_warning "Vulnerabilities found (check report)"
    fi

    npm audit || true

    cd ..
    echo "Report: security-reports/npm-audit-report.json"
}

# Main function
main() {
    mkdir -p security-reports

    case "${1:-all}" in
        --owasp)
            run_owasp
            ;;
        --trivy)
            run_trivy
            ;;
        --sonar)
            run_sonar
            ;;
        --npm)
            run_npm_audit
            ;;
        --all|"")
            run_owasp
            run_npm_audit
            run_trivy
            echo ""
            print_header "Security Scan Summary"
            echo "All scans completed. Check the following reports:"
            echo "  - OWASP: target/dependency-check-report.html"
            echo "  - npm:   security-reports/npm-audit-report.json"
            echo "  - Trivy: security-reports/trivy/"
            ;;
        --help|-h)
            echo "Usage: $0 [--owasp|--trivy|--sonar|--npm|--all|--help]"
            echo ""
            echo "Options:"
            echo "  --owasp   Run OWASP Dependency Check"
            echo "  --trivy   Run Trivy container scan"
            echo "  --sonar   Run SonarQube analysis"
            echo "  --npm     Run npm audit for frontend"
            echo "  --all     Run all scans (default)"
            echo "  --help    Show this help"
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
}

main "$@"

