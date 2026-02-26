# Event Service

Production-ready Spring Boot 3 microservice for Azure AKS: events and seat availability. Validates JWT from auth-service; ADMIN required for creating events.

## Stack

- **Java 17**, **Maven**, **JAR**
- Spring Web, Spring Data JPA, Spring Security (JWT validation only)
- PostgreSQL (dedicated DB – no shared access with auth-service), Lombok, Springdoc OpenAPI
- Actuator (health, liveness, readiness)

## Project structure

```
event-service/
├── src/main/java/com/booking/event/
│   ├── EventServiceApplication.java
│   ├── config/           # JWT validation, Security, OpenAPI, DB readiness
│   ├── controller/       # REST API
│   ├── dto/              # Request/response DTOs
│   ├── exception/        # Global handler, custom exceptions
│   ├── model/            # Event entity (with @Version for optimistic locking)
│   ├── repository/       # EventRepository
│   └── service/          # EventService
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
├── k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── secret.example.yaml
├── Dockerfile
├── .dockerignore
└── pom.xml
```

## API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/events` | JWT + **ADMIN** | Create event |
| GET | `/api/events` | JWT | List events |
| GET | `/api/events/{id}` | JWT | Get event by ID |
| PUT | `/api/events/{id}/reserve` | JWT | Reserve seats (body: `{"quantity": n}`) |
| GET | `/actuator/health` | No | Health, liveness, readiness |
| GET | `/swagger-ui.html` | No | Swagger UI |

## Configuration (environment variables)

Required in production:

- `JWT_SECRET` – same as auth-service (min 32 chars), so event-service accepts auth-service tokens
- `JWT_ISSUER` – `auth-service` (default)
- `DATABASE_URL` – JDBC URL for **event_db** (separate from auth-service)
- `DATABASE_USERNAME`, `DATABASE_PASSWORD`

Optional: `SERVER_PORT` (default 8081), `JPA_DDL_AUTO`, `LOG_LEVEL`, `DB_POOL_SIZE`, `DB_SCHEMA`

No hardcoded secrets; use Kubernetes Secrets or Azure Key Vault.

## Features

- **Seat availability**: `totalSeats`, `availableSeats` on Event; reserve decrements with optimistic locking.
- **Optimistic locking**: `@Version` on Event; concurrent reserves get 409 on conflict.
- **Input validation**: Jakarta validation on DTOs; global exception handler returns proper HTTP status codes.
- **Logging**: Production-style pattern and levels in `application.yml`.

## Build and run

```bash
mvn clean package
export JWT_SECRET=your-secret-same-as-auth-service
export DATABASE_URL=jdbc:postgresql://localhost:5432/event_db
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
mvn spring-boot:run
# Or with dev profile (dev-only JWT default, ddl-auto=update):
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Docker and Kubernetes

```bash
docker build -t event-service:1.0.0 .
kubectl apply -f k8s/secret.example.yaml   # Edit: same JWT_SECRET as auth-service, event_db credentials
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

Liveness: `/actuator/health/liveness`  
Readiness: `/actuator/health/readiness`
