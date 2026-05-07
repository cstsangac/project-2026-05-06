# Interview demo prompt — Extended MVP (≈4–8 hours)

Use this when you have **more than a quick evening**: **separate User and Wallet services**, clearer **bounded contexts**, slightly richer tests and README — still **not** production scope.

## Purpose (interview narrative)

**Microservices-style** demo for **live streaming + gaming-style engagement + payment-adjacent wallet flows**: users hold a **wallet**, **top up** via **mock payment**, **send priced virtual gifts** during a **live session**; viewers receive **real-time** notifications; **per-stream gifting leaderboard** in Redis.

Aligns with phone screen themes: **payments / gaming / live streaming**.

## Hard constraints

- **Time-box: ~4–8 hours** total (after toolchains work).
- **No real payment provider** — mock gateway only.
- **No video stack** — `streamId` (UUID) = live room.
- **One real-time protocol:** **WebSocket** (do not implement SSE in the same demo).
- Optimize for **runnable `docker-compose`**, clear **README**, and **defensible architecture** in interview.

## Tech stack (must show in repo)

- **Java 17+** + **Spring Boot** (multiple services)
- **Apache Kafka** — producer + consumer path (gift event → Go)
- **PostgreSQL** — primary persistence
- **Redis** — cache and/or **sorted-set leaderboard** per `streamId`
- **REST** + **JWT** for public APIs
- **Docker + docker-compose** — Postgres, Redis, Kafka (KRaft single broker OK), all Java services + Go
- **Golang** — small service: Kafka consumer + WebSocket broadcast
- **Tests:** **unit** tests + **1–2 integration tests** on critical path (e.g. debit + gift persist + Kafka publish, or wallet API). **Testcontainers** if time allows; else document integration against Compose.

## Services

### 1. User & Auth Service (Spring Boot)

- Register / login (JWT)
- Basic profile
- Endpoints needed for identity in other flows (validate token or user lookup as you prefer for MVP)

### 2. Wallet Service (Spring Boot)

- Mock **top-up** (credits wallet)
- **Authoritative** balance + **transaction history** in Postgres
- **Deduct** on gift with **DB transaction**; clear error if insufficient funds
- Exposes internal HTTP API for Gift service (or gateway pattern — **document** choice)

### 3. Gift Service (Spring Boot)

- `POST /streams/{streamId}/gifts` — send gift
- Gift catalog: static enum + seed **or** small `gift_types` table
- Calls Wallet to **debit** (HTTP on Docker network is OK)
- Persists gift event (sender, `streamId`, gift type, amount, timestamp)
- Produces Kafka event `gift.sent`
- **Idempotency:** `Idempotency-Key` or `clientRequestId` + unique constraint

### 4. Notification Fan-out Service (Golang)

- Consumes `gift.sent`
- In-memory subscribers: `streamId -> WebSocket clients`
- Broadcasts compact JSON to viewers
- **No business rules**, **no database**

## Redis leaderboard (MVP rules)

- **Per `streamId`**, **all-time** within demo data (no time windows unless you finish early).
- Redis **sorted set** e.g. `leaderboard:{streamId}`; score = total amount **or** gift count — **pick one**, document it.
- Update **once** per successful gift (from Gift Service after debit succeeds, **or** from one Kafka consumer — avoid double updates).

## Architecture expectations

- **Clean layering** per service: `api` / `application` / `domain` / `infrastructure` (pragmatic, not over-engineered).
- **Logging:** correlation id (header) where reasonable.
- **Errors:** consistent REST error JSON.

## Inter-service communication (MVP)

- Public: REST + JWT.
- Internal: **HTTP** between services by Docker service names (no mesh).
- **Kafka** for async fan-out to Go notification service.

## Deliverables

- **`docker-compose.yml`** for Postgres, Redis, Kafka, User service, Wallet service, Gift service, Go service.
- **`README.md`:** overview; **Mermaid** architecture; run instructions; **curl** flow (register → login → top-up → gift → WS viewer); **API documentation**; short explanation of **Golang** service; **Out of scope** section.

## Out of scope

- Real PSP webhooks, PCI, settlements, refunds
- Kubernetes, service mesh, multi-region
- Full observability stack (unless time left)
- Admin UI, mobile clients, chat, moderation

## Compared to the 2–3h prompt

This version **splits User and Wallet** into separate deployables to show **bounded contexts** and **service-to-service** calls, at the cost of more Compose wiring and duplication of shared DTOs/clients (keep shared code minimal — OpenAPI or small client module if needed).
