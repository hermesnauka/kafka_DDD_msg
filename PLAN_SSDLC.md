# kafka_DDD_msg

@AGENTS.md

# Project Overview & AI Workspace Guide

Welcome to the **DDD & Kafka Educational Messaging Platform** project workspace. This file serves as the main entry point for AI development assistants (like Claude) to understand the project context and coding standards.

## Main Agent Blueprint
To understand the full architecture, Secure SDLC requirements, User Stories, and system design, please proceed directly to the main tracking document:
👉 **[Go to agents.md](./agents.md)**

## Core Tech Stack
*   **Backend:** Java 21, Spring Boot 3.x, Spring Cloud Stream (Kafka)
*   **Frontend:** React (TypeScript), TailwindCSS, WebSockets
*   **Event Broker:** Apache Kafka
*   **Database:** PostgreSQL (Transactional), Redis (Caching/Session)
*   **Security Framework:** Spring Security, OAuth2/OIDC, OWASP Top 10 compliance mitigation

## Guardrails for AI Generation
1. Always adhere strictly to the Domain-Driven Design (DDD) tactical patterns defined in `agents.md`.
2. Do not write any code without incorporating the S-SDLC security controls outlined in the plan.
3. Every business feature must have an accompanying educational component to visualize the DDD/Kafka flow.
