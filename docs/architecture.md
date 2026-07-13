# Task Manager AI Architecture

## Interaction diagram

```
discovery-server (Eureka, :8761)
        ^
        | all services below register here and discover each other by name
        |
config-server (:8888) ---- serves shared config to task-service, ai-service
        |
api-gateway (:8080) ---- single entry point for external traffic
        |
        +-- /api/tasks --> task-service (:8081) --- PostgreSQL + Flyway
        |                         |
        |                         | Feign call, Circuit Breaker + Retry
        |                         v
        +-- /api/ai -----> ai-service (:8082) ---- Spring AI -> Groq API
```

All services except `discovery-server` register with Eureka and pull shared
configuration from `config-server`. External traffic only goes through
`api-gateway`; `task-service` and `ai-service` are not exposed directly.

## Why it's built this way

- **Eureka + Spring Cloud Gateway** — a classic combo for service discovery
  and a single entry point: the client doesn't need to know service
  addresses/ports, routing is defined by path predicates.
- **Config Server (native backend)** — shared settings (circuit breaker
  limits, the Groq model) are kept out of individual services' code and
  can be changed without a rebuild.
- **OpenFeign + Resilience4j** — `task-service` calls `ai-service` through a
  declarative client; a circuit breaker and retry protect against cascading
  failures if the AI provider is unavailable or slow. When the circuit
  breaker is open, a fallback stub is used instead of an error to the user.
- **Spring AI** — abstracts direct Groq REST API calls behind a
  `ChatClient`, making it easier to swap the model/provider later
  (OpenAI, Anthropic, a local model, etc.) without touching business code.
- **Splitting task-service / ai-service** — the AI logic is deliberately
  moved into its own service: it has a different load profile (external
  calls to Groq, rate limiting) and scales independently from the CRUD
  logic.
- **Flyway** — DB schema migrations are versioned alongside the code;
  `ddl-auto: validate` in production keeps Hibernate from silently changing
  the schema.
- **Testcontainers** — `task-service` integration tests spin up a real
  PostgreSQL in Docker instead of relying on H2, reducing the risk of
  divergence between the test and production databases.
- **Prometheus + Grafana** — metrics (HTTP latency, JVM, circuit breaker
  state) are collected from all services via Actuator
  `/actuator/prometheus`.

## Possible future improvements

- Add asynchronous summarization via a queue (Kafka/RabbitMQ) so the user's
  response isn't blocked while waiting for the Groq call.
- Move authentication (Spring Security + JWT / OAuth2) to the Gateway level.
- Add distributed tracing (Micrometer Tracing + Zipkin/Tempo) for end-to-end
  request diagnostics through gateway → task-service → ai-service.
- Cache identical summarization requests (Redis).
