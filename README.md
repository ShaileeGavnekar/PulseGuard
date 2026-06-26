# PulseGuard 🛰️

A lightweight **API & website uptime monitor** built with Spring Boot. Register the
endpoints you care about; a scheduled background job pings each one on an interval,
records latency and up/down status, and exposes the full history through a secured REST API.

Built to demonstrate solid backend fundamentals: clean layered architecture, JWT-secured
REST, JPA persistence, scheduled jobs, resilient outbound HTTP, validation, consistent
error handling, OpenAPI docs, tests, and Docker.

![PulseGuard dashboard](docs/dashboard.png)

> Save a screenshot of the running dashboard (http://localhost:8081/dashboard.html) to
> `docs/dashboard.png` and this image will render on GitHub.

---

## Why this project

Most beginner portfolios are another to-do app or e-commerce clone. PulseGuard is a small
but *real* service: it does background work, talks to the outside world, handles failure
gracefully, and is multi-tenant (every user only sees their own monitors). That gives you
concrete things to talk about in an interview beyond plain CRUD.

---

## Tech stack

| Area | Choice |
|------|--------|
| Language / runtime | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6 + JWT (jjwt) |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL (prod) · H2 (dev & tests) |
| Outbound HTTP | Spring `RestClient` with timeouts |
| Scheduling | Spring `@Scheduled` |
| API docs | springdoc-openapi (Swagger UI) |
| Build / deploy | Maven · Docker · docker-compose |
| Testing | JUnit 5 · Mockito · MockMvc · spring-security-test |

---

## Architecture

A conventional, well-separated layering — easy to explain and easy to extend:

```
controller  → REST endpoints, validation, DTO <-> response mapping
   │
service     → business logic, ownership checks, transactions
   │
repository  → Spring Data JPA interfaces
   │
entity      → JPA-mapped domain (User, Monitor, CheckResult)

security/   → JwtService, JwtAuthenticationFilter, UserDetailsService, SecurityConfig
check/      → HealthCheckScheduler (the background pinger)
common/     → cross-cutting concerns: global exception handler, security utils
```

Key design decisions:

- **Stateless JWT auth.** No server-side sessions; each request carries a Bearer token.
  Sessions are `STATELESS` and CSRF is disabled — the correct posture for a token-only API.
- **DTOs at the edges.** Entities are never serialized directly; request/response records
  keep the API contract decoupled from the database schema.
- **Ownership enforced in the service layer.** Every monitor query is scoped to the
  authenticated user, so users can't read or modify each other's data (returns 404, not 403,
  to avoid leaking existence).
- **Resilient scheduler.** Each check is isolated in its own try/catch with HTTP timeouts,
  so one unreachable target never stalls or breaks the sweep.
- **Consistent error contract.** A single `@RestControllerAdvice` maps exceptions to a
  uniform JSON `ApiError` body with the right status code.

---

## Running it

> Requires JDK 17+ and Maven 3.9+. (Run `mvn -N wrapper:wrapper` once if you'd prefer a `./mvnw` wrapper.)

### Option A — fastest (in-memory H2, no database needed)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Then open the interactive API docs at **http://localhost:8080/swagger-ui.html**.

### Option B — with PostgreSQL via Docker

```bash
docker compose up --build
```

This starts PostgreSQL and the app together on **http://localhost:8080**.

---

## Try the API in 30 seconds

```bash
# 1. Register and capture the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@pulseguard.dev","password":"password123","displayName":"Demo"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2. Create a monitor
curl -X POST http://localhost:8080/api/monitors \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Example","url":"https://example.com"}'

# 3. Wait ~1 minute for the scheduler to run, then check uptime stats
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/monitors/1/stats
```

A ready-to-run `requests.http` file is included for the IntelliJ / VS Code REST client.

---

## API overview

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/auth/register` | Create an account, get a JWT | Public |
| POST | `/api/auth/login` | Log in, get a JWT | Public |
| POST | `/api/monitors` | Create a monitor | Bearer |
| GET | `/api/monitors` | List my monitors (paged) | Bearer |
| GET | `/api/monitors/{id}` | Get one monitor | Bearer |
| PATCH | `/api/monitors/{id}` | Partial update | Bearer |
| DELETE | `/api/monitors/{id}` | Delete a monitor | Bearer |
| GET | `/api/monitors/{id}/stats` | Uptime % and counts | Bearer |
| GET | `/api/monitors/{id}/checks` | Check history (paged) | Bearer |

---

## Tests

```bash
mvn test
```

Includes unit tests for the service layer (Mockito) and an end-to-end security/integration
test (`AuthFlowIT`) that registers a user, obtains a token, and exercises a protected
endpoint with and without it.

---

## Alerts

When a monitor changes state, PulseGuard fires an alert — on the transition only, so it
won't spam you every cycle:

- **UP/UNKNOWN → DOWN** sends a "down" alert.
- **DOWN → UP** sends a "recovered" alert (toggle with `pulseguard.alerts.notify-on-recovery`).

By default alerts are logged to the console. To push them to Slack, Discord, or any HTTP
endpoint, set a webhook URL — the payload includes both `text` (Slack) and `content`
(Discord) fields plus structured monitor details:

```bash
# e.g. test with https://webhook.site, or a Slack/Discord incoming webhook
ALERT_WEBHOOK_URL=https://hooks.slack.com/services/XXX/YYY/ZZZ mvn spring-boot:run
```

Delivery failures are caught and logged so a broken webhook never disrupts the check sweep.

---

