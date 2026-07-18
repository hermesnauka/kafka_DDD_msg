# app01_java_kafka_msg — Agent Instructions

Java 21 + Spring Boot 3.x + Spring Cloud Stream (Kafka) backend, React +
TypeScript frontend: a real-time chat app that visualizes its own DDD
aggregates and Kafka event lifecycle as a live educational side-channel.
See `../AGENTS.md` for the repo-wide workflow and invariants.

Full requirements, security requirements, STRIDE threat model, wire
protocol, directory layout, and test strategy live in
[`PLAN_SSDLC.md`](./PLAN_SSDLC.md) — read it before touching this app's
code, and update it (with human review) before changing scope, the wire
protocol, or the threat model.

## App-specific rules

- **Domain layer stays framework-free.** `domain/` packages under
  `identity/`, `chatdelivery/`, and `analyticseducation/` must not import
  Spring, Kafka, or JPA types (NFR-Arch-1, PLAN_SSDLC.md §11).
- **Room membership is re-checked server-side on every access.** Never
  trust a client-supplied room ID alone for reads, message history,
  or WebSocket subscriptions (SR-5, FR-5, PLAN_SSDLC.md §7-8).
- **Telemetry carries metadata only, never message content.** Anything
  published to `educational.telemetry` is limited to
  topic/partition/offset/consumer-lag/timing fields (FR-6; see the
  information-disclosure row in PLAN_SSDLC.md §8).
- **Sandbox Mode never touches production consumer groups.** Pause/resume
  actions (FR-4) are scoped to a session-local sandbox topic/consumer
  group; they must never delay or block another user's real message
  delivery on `chat.room.events` (SR-9).
- **Never log message content or credentials.** Audit logs (SR-8) record
  who/what/when for security-relevant events, not chat payloads, passwords,
  or tokens.
- **Verify against Testcontainers first, docker-compose second.**
  Integration tests spin up real Kafka/PostgreSQL/Redis via Testcontainers
  (PLAN_SSDLC.md §10) so they're hermetic and reproducible; treat a full
  `docker-compose up` run as the final manual check before calling a
  milestone done, not the only check.
