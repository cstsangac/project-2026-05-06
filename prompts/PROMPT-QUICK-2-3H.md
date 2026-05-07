# Interview demo prompt — Quick MVP (≈2–3 hours)

Use this when you need a **runnable demo fast**. Scope is trimmed: **User + Wallet live in one Spring Boot service** so you still show **Java, Postgres, Redis, Kafka, REST, JWT, Docker Compose**, plus a **small Go consumer** for WebSocket fan-out.

## Purpose (interview narrative)

Small **microservices-style** demo aligned with **live streaming + gaming-style engagement + payment-adjacent wallet flows**: users **register/login**, hold a **wallet**, **top up** via a **mock gateway**, **send priced virtual gifts** during a session; viewers get **real-time** gift notifications; optional **per-stream leaderboard** in Redis.

## Hard constraints

- **Time-box: ~2–3 hours** of focused work after environment is ready.
- **No real PSP** — mock top-up only (synchronous success is fine).
- **No video** — `streamId` (UUID string) represents a “live room”.
- **One real-time protocol:** **WebSocket** only (no SSE in scope).
- **Tests:** a **small** set — e.g. **unit tests** for wallet/gift logic + **one** integration test for the happiest path (or skip Testcontainers; document manual verification).
- **Clean architecture:** **light** layers per app (`api` / `application` / `domain` / `infrastructure`) — avoid heavy abstractions.

## Tech stack (must appear in repo)

- **Java 17+**, **Spring Boot**
- **PostgreSQL** (authoritative wallet + users + gift rows)
- **Redis** (leaderboard sorted set **or** simple cache — **pick one** if time is tight; leaderboard is optional for 2h)
- **Apache Kafka** — Gift flow produces `gift.sent`; Go service consumes
- **REST** + **JWT** for public APIs
- **Docker + docker-compose** — Postgres, Redis, Kafka (single broker / KRaft OK), both services
- **Golang** — notification fan-out only

## Services (two backends + infra)

### 1. User & Wallet Service (Spring Boot) — **merged**

Single deployable JAR/module name e.g. `user-wallet-service`.

- **Auth:** register, login, JWT issuance, minimal profile (display name optional).
- **Wallet:** balance in Postgres; **mock top-up** endpoint; **transaction** rows (credit/debit).
- **Gift send (in-process or internal REST):** validate gift type/price (enum + seed **or** tiny `gift_types` table); **debit wallet in one DB transaction**; persist `gift_events` (sender, `streamId`, type, amount, timestamp); **publish** `gift.sent` to Kafka.
- **Idempotency (minimal):** `Idempotency-Key` header **or** `clientRequestId` with unique constraint — enough to discuss retries in interview.
- **Leaderboard (optional if clock runs out):** Redis sorted set `leaderboard:{streamId}` updated after successful gift; otherwise stub endpoint returning empty.

### 2. Notification Fan-out Service (Golang) — **small**

- Consume `gift.sent` from Kafka.
- In-memory map: `streamId -> []*websocket.Conn` (or equivalent).
- **Broadcast** JSON to all subscribers on that stream.
- **No** wallet rules, **no** DB — parse, validate JSON, fan-out only.

**Public API for viewers:** e.g. `GET /ws?streamId=...` (upgrade). Auth for WS **optional** in MVP (document as limitation).

## Inter-service / event flow

1. Client: JWT login → mock top-up → `POST /streams/{streamId}/gifts` on **User & Wallet Service**.
2. Service debits wallet, writes gift row, produces Kafka.
3. **Go** consumes event, pushes to WebSocket clients for that `streamId`.

## Deliverables

- `docker-compose.yml` starts: Postgres, Redis, Kafka, **User & Wallet Service**, **notification-go**.
- **`README.md`:** overview; **Mermaid** architecture diagram; `curl` + WS example; **API table**; **Out of scope** (real PSP, K8s, etc.).

## Out of scope (explicit)

- Real payment webhooks, PCI, refunds
- Separate User vs Wallet services, API gateway, service mesh
- SSE, mobile apps, admin UI, video
- Full test pyramid, Testcontainers (unless time remains)
- Production-grade exactly-once / DLQ (mention as future work in README)

## Interview line

“We merged identity and wallet **to ship a credible vertical slice in hours**, while keeping **Kafka + Go** for the async, bursty fan-out path you’d use under live load.”
