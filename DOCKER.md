# TrustMart E-commerce - Docker Setup

This guide explains how to run the entire TrustMart microservices application using Docker.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose v2.x or higher
- At least 8GB RAM allocated to Docker
- **Existing Keycloak setup** (with realm, clients, roles, and users already configured)

## Quick Start

### 1. Start Your Existing Keycloak Containers

First, start your existing Keycloak and PostgreSQL containers:

```bash
docker start keycloak-postgres keycloak
```

Verify they are running:
```bash
docker ps | grep keycloak
```

### 2. Build and Start All Services

```bash
docker-compose up --build
```

This will start all services:
- **MySQL** (port 3307) - Database for products and commands
- **Discovery Service** (port 8761) - Eureka service registry
- **Gateway Service** (port 8083) - API Gateway
- **Product Service** (port 8081) - Product management
- **Command Service** (port 8082) - Order management
- **Frontend** (port 8084) - React application

**Note:** Keycloak (port 8080) uses your existing container with all configurations preserved.

### 3. Access the Application

| Service | URL |
|---------|-----|
| Frontend | http://localhost:8084 |
| API Gateway | http://localhost:8083 |
| Keycloak Admin | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |

### 4. Default Credentials

Your existing Keycloak credentials apply. Default admin:
- Username: `admin`
- Password: `admin`


## Docker Commands

### Start all services
```bash
docker-compose up -d
```

### Stop all services
```bash
docker-compose down
```

### Rebuild a specific service
```bash
docker-compose up --build product-service
```

### View logs
```bash
docker-compose logs -f gateway-service
```

### Remove all containers and volumes
```bash
docker-compose down -v
```

## Service Dependencies

```
                    ┌─────────────────┐
                    │    Frontend     │
                    │   (port 8084)   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  Gateway Service │
                    │   (port 8083)    │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼───────┐   ┌───────▼───────┐   ┌───────▼───────┐
│Product Service│   │Command Service│   │   Keycloak    │
│  (port 8081)  │   │  (port 8082)  │   │  (port 8080)  │
└───────┬───────┘   └───────┬───────┘   └───────┬───────┘
        │                   │                   │
        └─────────┬─────────┘                   │
                  │                             │
          ┌───────▼───────┐             ┌───────▼───────┐
          │     MySQL     │             │   PostgreSQL  │
          │  (port 3307)  │             │  (Keycloak)   │
          └───────────────┘             └───────────────┘
                  │
          ┌───────▼───────┐
          │   Discovery   │
          │   (Eureka)    │
          │  (port 8761)  │
          └───────────────┘
```

## Troubleshooting

### Services not starting
Check if all dependencies are healthy:
```bash
docker-compose ps
```

### Database connection issues
Ensure MySQL is fully started before other services:
```bash
docker-compose logs mysql
```

### Keycloak issues
Check Keycloak logs:
```bash
docker-compose logs keycloak
```

## Environment Variables

You can override default configuration by creating a `.env` file in the root directory:

```env
MYSQL_ROOT_PASSWORD=your_password
KEYCLOAK_ADMIN_PASSWORD=your_admin_password
GATEWAY_SECRET=your_secret_key
```

