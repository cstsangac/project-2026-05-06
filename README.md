# project-2026-05-06 — Live Gift / Wallet Demo

Microservices-style interview demo for **live streaming + gaming-style engagement + payment-adjacent wallet flows**.

- **Java (Spring Boot)**: Auth (JWT), Wallet (top-up + debit), Gift sending, Kafka producer, optional Redis leaderboard
- **Golang**: Kafka consumer + WebSocket fan-out to viewers
- **Infra**: PostgreSQL, Redis, Kafka, Docker Compose

## Architecture

```mermaid
flowchart LR
  Client[Client / Viewer] -->|REST + JWT| UWS[user-wallet-service\nSpring Boot]
  Client -->|WebSocket /ws?streamId=...| NS[notification-service-go\nGolang]

  UWS -->|SQL| PG[(PostgreSQL)]
  UWS -->|ZINCRBY (optional)| R[(Redis)]
  UWS -->|produce gift.sent| K[(Kafka)]

  NS -->|consume gift.sent| K
  NS -->|broadcast JSON| Client
```

## Quickstart (local)

Start everything:

```powershell
docker compose up --build
```

Services:
- `user-wallet-service`: `http://localhost:8080`
- `notification-service-go`: `http://localhost:8090`

## Demo flow (REST + WebSocket)

Pick a stream id:

```powershell
$streamId = [guid]::NewGuid().ToString()
```

Register:

```powershell
curl -s http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"demo\",\"password\":\"pw\"}"
```

Login (grab token):

```powershell
$token = (curl -s http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"demo\",\"password\":\"pw\"}' | ConvertFrom-Json).accessToken
```

Top up wallet:

```powershell
curl -s http://localhost:8080/api/wallet/topup `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d "{\"amount\":100.00}"
```

Connect a viewer to WebSocket:

- **Option A (browser devtools console)**: open any page and run:
  - `new WebSocket("ws://localhost:8090/ws?streamId=<STREAM_ID>")`
- **Option B (wscat, optional)**:

```powershell
npx wscat -c \"ws://localhost:8090/ws?streamId=$streamId\"
```

Send a gift:

```powershell
curl -s http://localhost:8080/api/streams/$streamId/gifts `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d "{\"giftType\":\"ROSE\",\"clientRequestId\":\"req-1\"}"
```

Leaderboard (optional, Redis):

```powershell
curl -s http://localhost:8080/api/streams/$streamId/leaderboard `
  -H "Authorization: Bearer $token"
```

## API summary

- `POST /api/auth/register` — `{ username, password }`
- `POST /api/auth/login` — `{ username, password }` → `{ accessToken }`
- `GET /api/wallet` — current wallet + recent tx
- `POST /api/wallet/topup` — `{ amountCents }`
- `POST /api/wallet/topup` — `{ amount }` (decimal, scale=2)
- `POST /api/streams/{streamId}/gifts` — `{ giftType, clientRequestId? }` (idempotent if `clientRequestId` reused)
- `GET /api/streams/{streamId}/leaderboard` — top 10 by total gifted **minor units** (member = `userId`)

## Prompts

Implementation prompts live under `prompts/`:
- [`prompts/PROMPT-QUICK-2-3H.md`](prompts/PROMPT-QUICK-2-3H.md)
- [`prompts/PROMPT-EXTENDED-4-8H.md`](prompts/PROMPT-EXTENDED-4-8H.md)

## Tests

Default unit tests run with:

```powershell
cd services/user-wallet-service
mvn test
```

There is an **opt-in** Testcontainers smoke test (Postgres) + Embedded Kafka. Enable it with:

```powershell
$env:RUN_TESTCONTAINERS = \"true\"
mvn test
```

