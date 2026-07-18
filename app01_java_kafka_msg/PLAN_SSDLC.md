# app01_java_kafka_msg — SSDLC / PRD Plan

Status: **draft v1** — pending human review before implementation.

## 1. Overview

A multi-user, real-time messaging application that doubles as a **living
educational platform**: as users chat, the UI visualizes the engineering
concepts happening behind the scenes — how Domain-Driven Design (DDD)
constructs (Aggregates, Domain Events) interact with Apache Kafka (Topics,
Partitions, Consumer Groups) to deliver each message. Backend: Java 21 +
Spring Boot 3.x + Spring Cloud Stream (Kafka). Frontend: React + TypeScript.
Storage: PostgreSQL (transactional) + Redis (cache/session). This plan
expands the blueprint already sketched in `AGENTS.md` with the pieces an
SSDLC plan needs before code is written: a STRIDE threat model, the wire
protocol (REST/WebSocket/Kafka contracts), a concrete directory structure,
and a test strategy.

## 2. Scope

**In scope (v1)**
- User registration/login, JWT-based session.
- Direct messages and group chat rooms, delivered in real time over
  WebSockets, published/consumed through Kafka.
- The educational dashboard: DDD aggregate state transitions and the Kafka
  event lifecycle (producer → topic → partition → consumer group → UI),
  live, per message sent.
- Sandbox Mode: pause/resume a consumer to visually demonstrate backlog
  buildup and eventual consistency — scoped so it can never affect real
  message delivery for other users (see SR-9, STRIDE below).

**Out of scope (v1)**
- Federation with external chat protocols/systems.
- Message editing/deletion, read receipts beyond delivered/polled state.
- Multi-region/multi-cluster Kafka deployment.
- Mobile native clients (web only).

## 3. User Stories

### Epic 1: Secure Multi-User Messaging
- **US-1.1 Secure Authentication** — As a security-conscious user, I want to
  register and authenticate using an encrypted password mechanism, so my
  messages are protected from unauthorized access. *AC:* passwords hashed
  with BCrypt; failed logins throttled to resist brute-force.
- **US-1.2 Real-Time Messaging** — As an active communicator, I want to send
  a message and see it appear instantly for the recipient, so we can have a
  seamless conversation. *AC:* messages are published to a Kafka topic and
  pushed to the recipient's UI via WebSocket within 200ms.
- **US-1.3 Group Chat** — As a group of users, we want a shared chat room
  so we can hold a group conversation, not just 1:1 messages. *AC:* a
  `ChatRoom` supports 2+ participants; only participants can read or
  receive its messages (see SR-5).

### Epic 2: DDD & Kafka Educational Visualization
- **US-2.1 Domain Event Inspection** — As an aspiring software architect, I
  want to see a visual breakdown of the domain events fired when I hit
  "Send", so I understand how DDD aggregates publish events. *AC:* the UI
  shows a timeline mapping `ChatRoom` aggregate state changes
  (`PENDING → DELIVERED`) alongside each generated domain event's payload
  (`MessageSent` → `MessagePolled` → `MessageDelivered`).
- **US-2.2 Kafka Topic & Consumer Group Visualization** — As a developer
  learning event-driven architecture, I want to see which partition my
  message landed on and how the consumer group picked it up, so I
  understand Kafka's scaling/parallelism. *AC:* a real-time graph shows
  `chat.room.events`, partitions, offsets, and active consumer instances.
- **US-2.3 Sandbox Mode** — As a learner, I want to manually pause a
  consumer and watch the backlog grow, then resume it and watch the queue
  drain, so I understand eventual consistency hands-on. *AC:* pausing
  affects only my own sandbox session's consumer group, never another
  user's live message delivery (SR-9).

## 4. Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-1 | User registration, secure login, and profile management (Identity context). |
| FR-2 | Real-time direct messages and group chat, delivered via WebSocket, backed by Kafka. |
| FR-3 | Interactive DDD/Kafka dashboard: aggregate state transitions + Kafka event lifecycle, live, per message. |
| FR-4 | Sandbox Mode: pause/resume a *sandbox-scoped* consumer to demonstrate backlog buildup and eventual consistency. |
| FR-5 | A user can only read, list, or subscribe to chat rooms they are a participant of. |
| FR-6 | Educational telemetry (topic/partition/offset/consumer lag) is visualized without exposing message content to the Analytics & Education context. |

## 5. Security Requirements

| ID | Requirement |
|----|-------------|
| SR-1 | Passwords hashed with BCrypt (cost ≥ 12); failed login attempts rate-limited/throttled per account and per source IP. |
| SR-2 | JWT-based stateless auth: short-lived access tokens, refresh-token rotation, tokens delivered via `HttpOnly`, `Secure`, `SameSite` cookies — never exposed to JavaScript/localStorage. |
| SR-3 | All external traffic over HTTPS/WSS. Kafka broker traffic encrypted (TLS) with SASL/SCRAM authentication for every producer/consumer; no unauthenticated broker access. |
| SR-4 | Server-side input validation on every API input (Spring Bean Validation `@Valid`); output relies on React's default escaping (no `dangerouslySetInnerHTML` on chat content); all persistence queries parameterized (no string-built SQL). |
| SR-5 | Authorization enforced per chat-room membership on every read, list, and WebSocket subscription — a user must never be able to access a room's messages without being a participant (FR-5). |
| SR-6 | CI gates: SonarQube quality gate (0 critical/blocker), Snyk/OWASP Dependency-Check (0 High/Critical CVEs), GitGuardian secret scanning, CodeQL SAST. |
| SR-7 | Container/IaC security gates before deploy: Trivy image scan, Checkov IaC scan, SSL Labs check on the public TLS endpoint. |
| SR-8 | Structured audit logging of security-relevant events (auth attempts/failures, room-access denials, sandbox pause/resume) — logs never include message content or credentials. |
| SR-9 | Sandbox Mode (FR-4) is isolated from production message delivery: it operates on a dedicated sandbox topic/consumer group (or a strictly bounded per-session partition), and pausing it can never delay or block another user's real chat messages. |
| SR-10 | WebSocket connections are authenticated (JWT validated at handshake); room subscriptions are re-checked against current membership server-side, not trusted from the client. |

## 6. Architecture Overview

```
┌──────────────┐   REST + WS (STOMP)   ┌────────────────────────┐
│    React     │ <-------------------> │      Spring Boot        │
│  frontend    │      HTTPS/WSS        │   (Identity, Chat        │
│ (chat UI +   │                       │    Delivery, Analytics  │
│  education   │                       │    & Education contexts)│
│  dashboard)  │                       └───────┬────────┬───────┘
└──────────────┘                               │        │
                                     produces/  │        │ reads/writes
                                     consumes   │        │
                                       ┌────────▼──┐  ┌──▼──────────┐
                                       │  Apache    │  │ PostgreSQL   │
                                       │  Kafka     │  │ + Redis      │
                                       │ (SASL/TLS) │  │              │
                                       └────────────┘  └──────────────┘
```

The Analytics & Education bounded context only *consumes* events (from
`chat.room.events` metadata and `educational.telemetry`) — it is never in
the critical path of message delivery (NFR-Arch-1). The frontend never
talks to Kafka or the datastores directly.

## 7. Wire Protocol

### 7.1 REST API (base path `/api/v1`)

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/v1/auth/register` | Register a new user. |
| POST | `/api/v1/auth/login` | Authenticate; sets `HttpOnly` access/refresh cookies. |
| POST | `/api/v1/auth/refresh` | Rotate the access token using the refresh cookie. |
| GET | `/api/v1/users/me` | Current user's profile. |
| GET | `/api/v1/rooms` | List chat rooms the current user is a participant of (SR-5). |
| POST | `/api/v1/rooms` | Create a chat room (direct or group). |
| GET | `/api/v1/rooms/{roomId}/messages` | Paginated message history (participants only). |
| POST | `/api/v1/rooms/{roomId}/messages` | Send a message (publishes `MessageSent`). |
| POST | `/api/v1/sandbox/{sandboxSessionId}/pause` | Pause the caller's sandbox consumer (SR-9). |
| POST | `/api/v1/sandbox/{sandboxSessionId}/resume` | Resume it. |

### 7.2 WebSocket (STOMP over WSS)

| Destination | Direction | Purpose |
|-------------|-----------|---------|
| `/app/rooms/{roomId}/send` | client → server | Alternative low-latency send path (mirrors the REST POST). |
| `/topic/rooms/{roomId}` | server → client | Message delivery to room participants (subscription re-validated per SR-10). |
| `/topic/education/{roomId}` | server → client | Live DDD/Kafka visualization events for US-2.1/US-2.2 (aggregate transitions, partition/offset/consumer-lag updates). |

### 7.3 Kafka Topics

| Topic | Partitioning | Payload (JSON) |
|-------|--------------|-----------------|
| `chat.room.events` | by `roomId` (ordering within a room) | `{ "eventType": "MessageSent\|MessagePolled\|MessageDelivered", "roomId", "messageId", "senderId", "occurredAt" }` — **never the message body itself** for `MessagePolled`/`MessageDelivered` (only `MessageSent`'s consumer, the delivery service, needs the body; it is not re-published downstream). |
| `educational.telemetry` | by `roomId` | `{ "topic", "partition", "offset", "consumerGroup", "lagMs", "eventType", "occurredAt" }` — metadata only, no message content (FR-6, STRIDE information-disclosure mitigation below). |

### 7.4 Domain Events (Chat Delivery context)

`MessageSent` (aggregate created, `PENDING`) → `MessagePolled` (a consumer
instance picked it up) → `MessageDelivered` (pushed to the recipient's UI,
aggregate → `DELIVERED`). *(`MessageSent` is added here to make US-2.1's
acceptance criteria coherent with the aggregate lifecycle; the original
blueprint's domain-event list only named `MessagePolled`/`MessageDelivered`.)*

## 8. STRIDE Threat Model

| Category | Threat | Mitigation |
|----------|--------|------------|
| Spoofing | A forged or stolen JWT is used to impersonate another user. | Short-lived access tokens, refresh-token rotation, `HttpOnly`/`Secure` cookies (SR-2); WebSocket handshake re-validates the token (SR-10). |
| Spoofing | An unauthenticated producer injects fake events onto `chat.room.events`. | SASL/SCRAM + TLS on the Kafka cluster; only the backend service holds broker credentials, no direct external producer access (SR-3). |
| Tampering | Message content is tampered with in transit between browser and backend. | End-to-end TLS/WSS (SR-3). |
| Tampering | A user manipulates client-side Sandbox Mode controls to pause another user's *live* consumer group, disrupting their real chat delivery. | Sandbox consumer groups are isolated per session from production consumer groups; server-side authorization scopes pause/resume to the caller's own sandbox (SR-9). |
| Repudiation | No record of who accessed which room, or who paused/resumed a sandbox consumer. | Structured audit logging of auth events, room-access denials, and sandbox actions (SR-8). |
| Information Disclosure | A user subscribes to or queries a room they aren't a participant of. | Per-room authorization check on every read/list/subscribe, re-validated server-side (SR-5, FR-5). |
| Information Disclosure | `educational.telemetry` inadvertently carries message content (not just metadata), leaking private chat content to the Analytics & Education context or any future consumer of that topic. | Telemetry events are schema-constrained to metadata only — topic/partition/offset/timing/lag, never the message body (FR-6, §7.3). |
| Denial of Service | A malicious/misbehaving client floods the send endpoint or opens excessive WebSocket connections. | Rate limiting on auth and message-send endpoints; per-user WebSocket connection caps. |
| Denial of Service | Sandbox Mode's intentional backlog-buildup feature is abused to exhaust broker storage. | Sandbox topic/partition is capped (max backlog size and pause duration) and fully separate from `chat.room.events` (SR-9). |
| Elevation of Privilege | An authenticated user accesses another user's profile/credentials via an insecure direct object reference on a user-management endpoint. | Authorization always keyed off the authenticated principal from the validated JWT, never a client-supplied user ID. |
| Elevation of Privilege | An XSS payload in a chat message executes in another user's browser and exfiltrates their session. | React's default escaping (no `dangerouslySetInnerHTML` on chat content), CSP headers, and `HttpOnly` cookies so JS can't read the token even if XSS occurs (SR-2, SR-4). |

## 9. Directory Structure (proposed)

```
app01_java_kafka_msg/
  PLAN_SSDLC.md
  AGENTS.md
  CLAUDE.md
  backend/
    build.gradle.kts (or pom.xml)
    src/main/java/.../identity/            # Identity bounded context
      domain/                              # User, Credentials (entities/VOs)
      application/                         # register/login/refresh use cases
      infrastructure/                      # JPA repos, Spring Security config
    src/main/java/.../chatdelivery/        # Chat Delivery bounded context
      domain/                              # ChatRoom (aggregate root),
                                            # Message/Participant (entities),
                                            # MessageContent/Timestamp/MessageId (VOs),
                                            # MessageSent/MessagePolled/MessageDelivered (events)
      application/                         # send/poll/deliver use cases
      infrastructure/                      # Kafka producer/consumer, STOMP controllers, JPA
    src/main/java/.../analyticseducation/  # Analytics & Education bounded context
      domain/
      application/                         # projections for the education dashboard
      infrastructure/                      # Kafka consumers (read-only), STOMP publisher
    src/main/resources/application.yml
    src/test/java/...
  frontend/
    src/
      features/auth/
      features/chat/
      features/education-dashboard/        # DDD/Kafka visualization (US-2.1, US-2.2)
      features/sandbox/                     # US-2.3
      api/                                  # typed REST + STOMP client
    package.json
  deploy/
    docker-compose.yaml                     # kafka, postgres, redis, backend, frontend (local dev)
  test/
    integration/                            # Testcontainers-backed
```

## 10. Test Strategy

- **Domain unit tests**: JUnit 5 + Mockito against the `domain/` packages
  only — no Spring context, no Kafka/DB, verifying NFR-Arch-1's isolation
  by construction (if a domain test needs Spring or Kafka to run, that's a
  layering violation).
- **Integration tests**: Testcontainers spinning up real Kafka, PostgreSQL,
  and Redis for producer/consumer and repository tests — hermetic and
  reproducible without a shared external environment.
- **API tests**: `@SpringBootTest` + `MockMvc`/`WebTestClient` for REST; a
  STOMP test client for the WebSocket destinations in §7.2.
- **Authorization regression tests**: explicit tests asserting a
  non-participant is denied on every room read/list/subscribe path (SR-5) —
  this is a security requirement, not just a feature test, and should fail
  CI if broadened.
- **Frontend unit tests**: Vitest/Jest + React Testing Library, mocking the
  REST/STOMP client.
- **Security gates (CI)**: SonarQube, Snyk/OWASP Dependency-Check,
  GitGuardian, CodeQL (SR-6); Trivy + Checkov before deploy (SR-7).
- **E2E (stretch)**: Playwright driving the full stack (frontend + backend
  + real Kafka/Postgres via Testcontainers/docker-compose) through
  send-message → see-visualization (US-1.2, US-2.1, US-2.2).

## 11. Non-Functional Requirements

- **NFR-Perf-1**: End-to-end message delivery (producer → consumer → UI)
  under 200ms at normal load.
- **NFR-Arch-1**: Complete separation of the domain layer from
  infrastructure (Kafka, JPA) — the domain model has no framework
  dependencies.
- Observability via Spring Boot Actuator, exported to Prometheus/Grafana
  (matches the S-SDLC maintenance-phase tooling in `AGENTS.md`).
- The Analytics & Education context is eventually consistent and
  read-only; it lagging or being briefly unavailable must never delay or
  block live chat delivery.

## 12. Open Questions

- `AGENTS.md`'s tech stack lists both JWT auth (NFR-Sec-1) and OAuth2/OIDC
  — are these complementary (OIDC login issuing the app's own short-lived
  JWT) or is OIDC meant to fully replace a homegrown JWT flow? This plan
  assumes the former; needs confirmation before Identity-context work
  starts.
- Should Sandbox Mode's isolation (SR-9) be a fully separate Kafka
  cluster/namespace, or is a dedicated topic + consumer-group-per-session
  on the same cluster an acceptable boundary for v1?
- Is a Helm chart / Kubernetes deployment target in scope for this app, or
  is `docker-compose` (local dev) the only deployment story for v1?
