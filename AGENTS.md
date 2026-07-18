# System Blueprint: DDD & Kafka Educational Messaging Platform

This document outlines the **Requirements**, **Secure SDLC Plan**, **User Stories**, and **Architectural Mapping** for the application. It acts as the single source of truth for engineering agents.

---

## 1. Project Vision & Core Goal
The objective is to build a multi-user, real-time messaging application that simultaneously acts as a **living educational platform**. As users exchange messages, the application visualizes behind-the-scenes engineering concepts in real time: how **Domain-Driven Design (DDD)** constructs (Aggregates, Domain Events) interact with **Apache Kafka** (Topics, Partitions, Consumers).

---

## 2. Secure SDLC (S-SDLC) Plan

We implement a security-first approach throughout the software development lifecycle.

| SDLC Phase | Activities & Security Touchpoints | Deliverables / Tools |
| :--- | :--- | :--- |
| **1. Requirements** | Define Security Requirements, Privacy Impact Assessment (PIA). | Abuse Cases, Compliance Checklists |
| **2. Design** | Threat Modeling (STRIDE methodology), Secure Architecture review. | Threat Model Diagram, Data Flow Diagrams |
| **3. Implementation** | Secure Coding Standards, IDE-based linting, Secret Scanning. | SonarQube, Snyk, GitGuardian |
| **4. Testing** | SAST, DAST, Dependency Vulnerability Scanning, Pen Testing. | GitHub Actions (CodeQL, OWASP Dependency-Check) |
| **5. Deployment** | Container Security, Infrastructure as Code (IaC) scanning, TLS configuration. | Trivy (Docker scan), Checkov (IaC), SSL Labs |
| **6. Maintenance** | Log Management, SIEM monitoring, Incident Response readiness. | Spring Boot Actuator, Prometheus, Grafana |

---

## 3. System Requirements

### 3.1. Functional Requirements (FR)
*   **FR-1: User Management:** Users must be able to register, log in securely, and manage profiles.
*   **FR-2: Real-time Messaging:** Users must be able to send, receive, and read direct messages and group chat messages instantly.
*   **FR-3: Interactive DDD/Kafka Dashboard:** A split-screen UI element that visualizes:
    *   The **DDD Aggregate state change** (e.g., `Message` aggregate transitioning from `PENDING` to `DELIVERED`).
    *   The **Kafka Event Lifecycle** (Producer -> Topic -> Partition -> Consumer Group -> React UI via WebSocket).
*   **FR-4: Sandbox Mode:** A step-by-step interactive mode where users can manually pause Kafka consumers to see how event backlogs build up, and then resume them to see eventual consistency in action.

### 3.2. Non-Functional & Security Requirements (NFR)
*   **NFR-Sec-1 (Authentication):** JWT-based stateless authentication with short-lived tokens and secure HTTP-Only cookies.
*   **NFR-Sec-2 (Data at Rest/Transit):** All communications must use HTTPS and WSS (Secure WebSockets). Kafka traffic must be encrypted using TLS, with SASL/SCRAM for authentication.
*   **NFR-Sec-3 (Input Validation):** Strict XSS and SQL Injection mitigation via Spring Validation API and React context escaping.
*   **NFR-Perf-1 (Latency):** End-to-end messaging delivery (Producer to Consumer) must be under 200ms under normal loads.
*   **NFR-Arch-1 (DDD Isolation):** Complete separation of Domain layer from Infrastructure layer. The domain model must have no dependencies on Kafka or database frameworks.

---

## 4. Agile User Stories

### Epic 1: Secure Multi-User Messaging
*   **User Story US-1.1: Secure Authentication**
    *   **As a** security-conscious user,
    *   **I want to** register and authenticate using an encrypted password mechanism,
    *   **So that** my personal messages are protected from unauthorized access.
    *   *Acceptance Criteria:* Passwords must be hashed using BCrypt. Failed login attempts must be throttled to prevent brute-force attacks.
*   **User Story US-1.2: Real-Time Messaging**
    *   **As a** active communicator,
    *   **I want to** send messages to another user and see them appear instantly,
    *   **So that** we can have a seamless conversation.
    *   *Acceptance Criteria:* Messages must be published to a Kafka topic and pushed to the recipient's UI via WebSockets within 200ms.

### Epic 2: DDD & Kafka Educational Visualization
*   **User Story US-2.1: Domain Event Inspection**
    *   **As an** aspiring software architect,
    *   **I want to** see a visual breakdown of the `MessageSent` Domain Event when I hit "Send",
    *   **So that** I understand how DDD aggregates publish events.
    *   *Acceptance Criteria:* The UI must show a timeline mapping the `ChatAggregate` state changes alongside the payload of the generated Domain Event.
*   **User Story US-2.2: Kafka Topic & Consumer Group Visualization**
    *   **As a** developer learning event-driven architecture,
    *   **I want to** see which Kafka partition my message landed on and how the consumer group picked it up,
    *   **So that** I understand Kafka's scaling and parallel processing features.
    *   *Acceptance Criteria:* The application must provide a real-time graph showing Kafka topics (`chat-messages`), partitions, offset numbers, and active consumer instances processing the messages.

---

## 5. Tactical DDD & Kafka Mapping Blueprint

To maintain clarity for development, the application's code structure will mirror this architectural blueprint:

### 5.1. Bounded Contexts
1.  **Identity Context:** Manages users, credentials, and tokens.
2.  **Chat Delivery Context:** Manages active chat rooms, message aggregation, and delivery status.
3.  **Analytics & Education Context:** Listens to all system events to build the educational visualization dashboard without interfering with the live chat performance.

### 5.2. Tactical DDD Constructs (Chat Delivery Context)
*   **Aggregate Root:** `ChatRoom`
*   **Entities:** `Message`, `Participant`
*   **Value Objects:** `MessageContent`, `Timestamp`, `MessageId`
*   **Domain Event:** `MessagePolled`, `MessageDelivered`

### 5.3. Kafka Topic Architecture
*   `chat.room.events`: Partitioned by `RoomId`. Ensures strict chronological ordering of messages within the same chat room.
*   `educational.telemetry`: Collects performance metrics, schema details, and processing lags to feed the React education panel.

---

## 6. S-SDLC Verification Checklist (Definition of Done)
- [ ] Code passes SonarQube quality gate with 0 critical/blocker vulnerabilities.
- [ ] Dependency check shows 0 High/Critical CVEs.
- [ ] Threat Model reviewed and updated for any new architectural changes.
- [ ] All inputs validated via Spring `@Valid` and sanitized against XSS.
- [ ] Data privacy rules verified (users cannot view messages from chat rooms they do not belong to).
