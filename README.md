# Trip Service

A core microservice in the Traversium platform responsible for trip management, media handling, and coordination with moderation and notification systems. The service exposes REST APIs and gRPC endpoints and is designed for scalable, multi-tenant operation.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Running the Service](#running-the-service)
- [API Documentation](#api-documentation)
- [Architecture](#architecture)
- [Database](#database)
- [Integration](#integration)
- [Monitoring and Health](#monitoring-and-health)

## Features

### Trip Management
- RESTful APIs for creating, updating, and managing trips
- Media association with trips, including batch media uploads
- Support for structured trip-related metadata

### Media Handling
- Batch media upload support
- Media metadata storage
- Integration with ModerationService for content validation

### Security
- Firebase Authentication integration
- Keycloak-based service-to-service security for gRPC communication
- JWT token validation
- Multi-tenancy support
- Tenant isolation

### Integration
- REST API endpoints
- gRPC communication with UserService and ModerationService
- Kafka-based event streaming for notifications and audit data
- Prometheus metrics for monitoring
- Swagger documentation with request and response examples

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Firebase project with service account credentials
- Keycloak (for service-to-service authentication)
- Kafka cluster (for event streaming)
- Docker (optional, for containerized deployment)

## Configuration

### Application Properties

The service is configured via `src/main/resources/application.properties`. Key configurations:

```properties
# Application
spring.application.name=TripService
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/trip_db
spring.datasource.username=<user>
spring.datasource.password=<password>

# Config Server (optional)
spring.config.import=optional:configserver:http://localhost:8888

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration/tenant

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.notification-topic=notification-topic
spring.kafka.audit-topic=audit-topic

# gRPC server (TripService)
spring.grpc.server.port=9091
spring.grpc.server.reflection-service-enabled=true
spring.grpc.server.metrics.enabled=true

# gRPC client (TripService -> ModerationService)
spring.grpc.client.moderation.host=localhost
spring.grpc.client.moderation.port=9090
```

### Kafka Configuration

Event streaming configuration for asynchronous communication:

- **`spring.kafka.bootstrap-servers`**: Kafka broker address for connecting to the Kafka cluster
- **`spring.kafka.notification-topic`**: Topic name for publishing notification events (album added to trip, etc.)
- **`spring.kafka.audit-topic`**: Topic name for publishing audit events of user action in TripService

### gRPC Configuration

gRPC client configuration for inter-service communication:

**TripService Server:**
- **`spring.grpc.server.port`**: Port of the TripService gRPC server for communication with UserService

**ModerationService Client:**
- **`spring.grpc.client.moderation.host`**: Hostname of the ModerationService gRPC server
- **`spring.grpc.client.moderation.port`**: Port of the ModerationService gRPC server (content moderation of TripService objects)

### Keycloak OAuth2 Configuration

Service-to-service authentication configuration for secure gRPC communication:

```properties
security.oauth2.client.token-uri=<issuer-uri>
security.oauth2.client.client-id=trip-service
security.oauth2.client.client-secret=<client-secret>
security.oauth2.client.grant-type=client_credentials
security.oauth2.client.refresh-skew-seconds=30
```

**Property Descriptions:**

- **`security.oauth2.client.token-uri`**: Keycloak endpoint URL for obtaining access tokens
    - Example: `http://localhost:8202/auth/realms/traversium/protocol/openid-connect/token`
- **`security.oauth2.client.client-id`**: Client ID registered in Keycloak for TripService (default: trip-service)
- **`security.oauth2.client.client-secret`**: Confidential client secret from Keycloak
- **`security.oauth2.client.grant-type`**: OAuth2 grant type for authentication
  - Use `client_credentials` for service-to-service (machine-to-machine) authentication
- **`security.oauth2.client.refresh-skew-seconds`**: Token refresh buffer time in seconds (default: 30)

### Resilience4j Configuration

Circuit breaker and retry mechanisms for gRPC calls (ModerationService):
```properties
resilience4j.circuitbreaker.instances.moderation-service.sliding-window-size=10
resilience4j.circuitbreaker.instances.moderation-service.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.moderation-service.wait-duration-in-open-state=30s
resilience4j.retry.instances.azureModeration.wait-duration=1s
resilience4j.retry.instances.azureModeration.max-attempts=2
```

- **`sliding-window-size`**: Number of calls to track for circuit breaker evaluation (default: 10)
- **`failure-rate-threshold`**: Percentage of failures that triggers circuit breaker to open (default: 50%)
- **`wait-duration-in-open-state`**: Wait time between retry attempts in an open state (default: 30 seconds)
- **`wait-duration`**: Wait time between retry attempts (default: 1 second)
- **`max-attempts`**: Maximum retry attempts for failed gRPC calls (default: 2)

## Running the Service

### Local Development

```bash
# Run with Maven
mvn spring-boot:run

# Or build and run JAR (e.g. version 1.3.0-SNAPSHOT)
mvn clean package
java -jar target/TripService-1.3.0-SNAPSHOT.jar
```

### Using Docker

```bash
# Build Docker image
docker build -t traversium-trip-service .

# Run container
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/trip_db \
  traversium-trip-service
```

### Verify Service is Running

```bash
# Health check
curl http://localhost:8081/actuator/health

# Liveness probe
curl http://localhost:8081/actuator/health/liveness

# Readiness probe
curl http://localhost:8081/actuator/health/readiness
```

## API Documentation

### REST API

Once the service is running, access the Swagger UI:

```
http://localhost:8081/swagger-ui.html
```

### Key Endpoints

**Trip Operations**
- `GET /rest/v1/trips` - Get trips for current tenant
- `GET /rest/v1/trips/{tripId}` - Get trip by ID
- `GET /rest/v1/trips/search` - Search trips by title
- `GET /rest/v1/trips/viewers` - Get viewed trips
- `GET /rest/v1/trips/owner/{ownerId}` - Get trips by owner
- `GET /rest/v1/trips/collaborators/{collaboratorId}` - Get trips by collaborator
- `POST /rest/v1/trips` - Create a new trip
- `POST /rest/v1/trips/autosort` - Autosort trip media
- `PUT /rest/v1/trips` - Update a trip
- `DELETE /rest/v1/trips/{tripId}` - Delete trip

**Album Operations**
- `GET /rest/v1/albums` - Get all albums
- `GET /rest/v1/albums/{albumId}` - Get album
- `GET /rest/v1/trips/{tripId}/albums/{albumId}` - Get album from trip
- `POST /rest/v1/trips/{tripId}/albums` - Add album to trip
- `PUT /rest/v1/albums/{albumId}` - Update album
- `DELETE /rest/v1/trips/{tripId}/albums/{albumId}` - Delete album from trip

**Media Operations**
- `GET /rest/v1/media` - Get all media
- `GET /rest/v1/media/{mediaId}` - Get media
- `GET /rest/v1/media/path/{pathUrl}` - Get media by path URL
- `GET /rest/v1/media/uploader/{uploaderId}` - Get media by uploader
- `GET /rest/v1/trips/{tripId}/media` - Get all media from trip
- `GET /rest/v1/albums/{albumId}/media/{mediaId}` - Get media from album
- `PUT /rest/v1/albums/{albumId}/media` - Add media to album
- `DELETE /rest/v1/albums/{albumId}/media/{mediaId}` - Delete media from album

**Collaborator Operations**
- `PUT /rest/v1/trips/{tripId}/collaborators/{collaboratorId}` - Add collaborator to trip
- `DELETE /rest/v1/trips/{tripId}/collaborators/{collaboratorId}` - Remove collaborator from trip

**Viewer Operations**
- `PUT /rest/v1/trips/{tripId}/viewers/{viewerId}` - Add viewer to trip
- `DELETE /rest/v1/trips/{tripId}/viewers/{viewerId}` - Remove viewer from trip

## Architecture

### Multi-Tenancy

The service implements schema-based multi-tenancy using the `common-multitenancy` library. Each tenant has an isolated database schema.

### Security

- **Firebase Authentication**: All requests must include a valid Firebase ID token in the Authorization header
- **Tenant Filter**: Extracts and validates tenant context from request headers
- **Principal**: User context is available via `TraversiumPrincipal` in secured endpoints
- **Keycloak**: Service uses OAuth2 client credentials flow via Keycloak for secured service-to-service communication

### Event-Driven Architecture

The service publishes events to Kafka:
- **Trip Events**: Trip creation, updates, deletion
- **Album Events**: Album creation, updates, deletion
- **Media Events**: Media addition, deletion
- **Notification Events**: Trigger notifications for trip collaborators (including owner)
- **Audit Events**: Track user actions for compliance

### Resilience Patterns

- **Circuit Breaker**: Prevents cascading failures when calling ModerationService
- **Retry**: Automatic retry for transient failures
- **Fallback**: Graceful degradation when dependencies are unavailable

## Database

### Schema Management

Database migrations are managed by Flyway. Migration scripts are located in:
```
src/main/resources/db/migration/tenant/
```

## Integration

### gRPC Clients

**ModerationService Client** (`src/main/kotlin/travesium/tripservice/service/ModerationServiceGrpcClient.kt`):
- Content moderation for trip content
- Protected by circuit breaker and retry

### Kafka Integration

**Producers:**
- Trip events (topic: configured in KafkaProperties)
- Notification events
- Audit events

**Configuration:** See `src/main/kotlin/travesium/tripservice/kafka/KafkaConfig.kt`

## Monitoring and Health

### Health Checks

- **Liveness**: `/actuator/health/liveness` - Indicates if the application is running
- **Readiness**: `/actuator/health/readiness` - Indicates if the application is ready to serve traffic
- **Database**: `/actuator/health/db` - Database connectivity check

### Metrics

Prometheus metrics exposed at:
```
http://localhost:8081/actuator/prometheus
```

Key metrics:
- JVM metrics (memory, threads, GC)
- HTTP request metrics
- Database connection pool metrics
- Circuit breaker state
- Custom business metrics

### Logging

Logs are structured in JSON format (Logstash encoder) for ELK Stack integration:
- Application logs: Log4j2
- Request/response logging
- Error tracking
