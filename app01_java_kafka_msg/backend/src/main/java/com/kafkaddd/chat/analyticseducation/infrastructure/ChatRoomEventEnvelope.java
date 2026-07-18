package com.kafkaddd.chat.analyticseducation.infrastructure;

import java.time.Instant;

/**
 * This context's own view of the {@code chat.room.events} wire schema
 * (PLAN_SSDLC.md §7.3) — deliberately NOT the same Java type as Chat
 * Delivery's {@code chatdelivery.infrastructure.ChatEventEnvelope} (which
 * is package-private there anyway): each bounded context models a shared
 * wire contract on its own terms rather than importing another context's
 * internal type.
 *
 * <p>Notably, this record has no {@code senderId}/{@code content} fields at
 * all, even though the real {@code MessageSent} JSON on the wire carries
 * them. Jackson silently ignores JSON properties with no matching field
 * (Spring Boot's default), so message content is excluded *structurally* —
 * there's no field to accidentally populate, log, or forward — rather than
 * relying on a filter step in code that a future change could forget
 * (FR-6).
 */
record ChatRoomEventEnvelope(String eventType, String roomId, String messageId, Instant occurredAt) {}
