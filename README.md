# Task Manager AI

A microservice-based task/note management application with automatic
AI summarization of content. Stack: **Java 21, Spring Boot 3, Spring Cloud
2023.x (Eureka, Config Server, Gateway, OpenFeign, Resilience4j), Spring AI +
Groq API, PostgreSQL, Flyway, Docker Compose, Prometheus/Grafana**.

Architecture details and design rationale are in [docs/architecture.md](docs/architecture.md).

## Modules

| Module            | Port | Purpose                                                   |
|--------------------|------|------------------------------------------------------------|
| discovery-server   | 8761 | Eureka - service registry                                  |
| config-server      | 8888 | Centralized configuration (native backend)                 |
| api-gateway        | 8080 | Single entry point, routing, circuit breaker, CORS         |
| task-service        | 8081 | Task/note CRUD, PostgreSQL + Flyway                         |
| ai-service          | 8082 | Text summarization via Spring AI / Groq                    |

## Quick start (Docker Compose)

1. Copy `.env.example` to `.env` and set your `GROQ_API_KEY`:
   ```bash
   cp .env.example .env
   ```
2. Build and start the whole environment:
   ```bash
   docker compose --env-file .env up --build
   ```
3. Available addresses once started:
   - API Gateway: http://localhost:8080
   - Eureka Dashboard: http://localhost:8761
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin)

## Running locally without Docker (for development)

Start PostgreSQL and export `GROQ_API_KEY`, then run the modules in this
order (each with `mvn spring-boot:run` in its own directory):

```
discovery-server → config-server → task-service, ai-service → api-gateway
```

## API usage example

Create a task:
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Prepare report","content":"Need to collect Q2 sales data, compare it with the plan, and send it to the manager by Friday."}'
```

> On Windows (git-bash/MinGW), if you send non-ASCII text, prefer putting the
> JSON body in a UTF-8 file and using `--data-binary @file.json`, since the
> console's active code page can otherwise corrupt multi-byte characters.

Request an AI summary for a created task (use the `id` from the response above):
```bash
curl -X POST http://localhost:8080/api/tasks/1/summarize
```

List tasks:
```bash
curl http://localhost:8080/api/tasks
```

## Testing

```bash
# Unit tests for all modules
mvn test

# task-service integration tests (spin up PostgreSQL via Testcontainers, requires Docker)
mvn -pl task-service verify
```

CI (`.github/workflows/ci.yml`) runs the build, unit and integration tests on
every push/PR, then builds Docker images for all services.

## Monitoring

All services expose metrics via Spring Boot Actuator (`/actuator/prometheus`).
Prometheus scrapes them per `monitoring/prometheus/prometheus.yml`, and
Grafana comes up with a ready-made dashboard (`monitoring/grafana/provisioning`)
showing RPS, latency, JVM heap usage, and the circuit breaker state for
task-service/ai-service.

## Fault tolerance

- `task-service → ai-service` is protected by Resilience4j **Circuit Breaker + Retry**:
  if the AI service is unavailable, the user's request isn't dropped - a clear
  fallback summary stub is returned instead.
- `api-gateway` also wraps its routes in a circuit breaker with its own
  fallback endpoints (`/fallback/tasks`, `/fallback/ai`).
- `ai-service` throttles calls to Groq with Resilience4j **RateLimiter**,
  to avoid hitting provider limits and getting an unexpected bill for
  overuse.
