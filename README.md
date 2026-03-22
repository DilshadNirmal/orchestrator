# DataSync Orchestrator

A flexible job orchestrator that syncs data from MongoDB sources to PostgreSQL using Debezium CDC and RabbitMQ.

## Architecture

MongoDB → Debezium (CDC) → RabbitMQ → App → PostgreSQL

## Tech Stack

- **Java 21** + **Spring Boot 3.4**
- **Debezium** - Change Data Capture
- **RabbitMQ** - Message Queue
- **PostgreSQL** - Target Database
- **Flyway** - Database Migrations

## Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven

## Setup

### 1. Start Infrastructure

```bash
docker-compose up -d
2. Build the App
./mvnw clean package
3. Run
./mvnw spring-boot:run
4. Access
- API: http://localhost:8080
- RabbitMQ UI: http://localhost:15672 (guest/guest)
- Health: http://localhost:8080/actuator/health
API Endpoints
Sources
- GET /api/sources - List all sources
- POST /api/sources - Create source
- PUT /api/sources/{id} - Update source
- DELETE /api/sources/{id} - Delete source
- POST /api/sources/{id}/sync - Trigger manual sync
- POST /api/sources/{id}/enable-cdc - Enable CDC
- POST /api/sources/{id}/disable-cdc - Disable CDC
Jobs
- GET /api/jobs - List jobs (paginated)
- GET /api/jobs/{id} - Get job details
Example: Create a Source
curl -X POST http://localhost:8080/api/sources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Production Users",
    "type": "mongodb",
    "config": {
      "mongodbUri": "mongodb://remote-server:27017",
      "database": "myapp",
      "collection": "users"
    },
    "enabled": true
  }'
License
MIT
```
