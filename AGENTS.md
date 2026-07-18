# kafka_DDD_msg — Agent Instructions

DDD & Kafka educational messaging platform: a multi-user, real-time chat app
that simultaneously visualizes, live, how Domain-Driven Design constructs
(Aggregates, Domain Events) interact with Apache Kafka (Topics, Partitions,
Consumer Groups) to deliver each message.
Each `appNN_*/` directory is a self-contained app. app01_java_kafka_msg's
design lives in `app01_java_kafka_msg/PLAN_SSDLC.md` — read it before
touching app01 code; don't duplicate it here.

## Workflow: build each app in these steps, in order

1. **Plan doc first.** Before any code, write (or update) an SSDLC/PRD plan
   markdown in the app directory — requirements (FR/SR), user stories,
   STRIDE threat model, wire protocol, directory structure, test strategy.
   The agent drafts it; the human reviews before step 2 starts.
2. **Full pipeline integration.** Wire the backend, frontend, and Kafka
   together behind the shared wire protocol; verify end-to-end against
   real Kafka/PostgreSQL/Redis (via Testcontainers for automated tests, or
   `docker-compose` for local/manual runs) — not by reading the code.

## Invariants (all apps)

- Every increment is verified by running something (unit test, a
  Testcontainers-backed integration test, a docker-compose smoke run) —
  not by reading the code.
- The domain layer has zero framework dependencies: no Spring, Kafka, or
  JPA imports inside `domain/` packages. If a change needs persistence or
  messaging, that belongs in `application/`/`infrastructure/`, not
  `domain/`.
- A user can never read, list, or subscribe to a chat room they are not a
  participant of — every access path re-checks membership server-side,
  never trusting a client-supplied ID alone.

## Conventions

- Keep this file and CLAUDE.md short and universal; put app-specific rules
  in a nested `appNN_*/AGENTS.md` scoped to that app.
- Build artifacts (`target/`, `build/`, `.gradle/`, `dist/`,
  `node_modules/`) never get committed — extend `.gitignore` when adding a
  new toolchain.
