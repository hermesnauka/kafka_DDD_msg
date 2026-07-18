package com.kafkaddd.chat.analyticseducation.domain;

import java.time.Instant;

/**
 * One point in a message's DDD-aggregate-state-change / Kafka-event-lifecycle
 * timeline (PLAN_SSDLC.md §7.3/US-2.1/US-2.2): which event fired, which
 * room/message it belongs to, and where it was observed in Kafka
 * (partition/offset/consumer group) — never the message content (FR-6).
 *
 * <p>Deliberately a plain record with no value objects wrapping the
 * individual fields: unlike {@code ChatRoom}/{@code User}, this context has
 * no business invariants of its own to protect (it's a read-only
 * projection, NFR-Arch-1/PLAN_SSDLC.md §6) — a {@code kafkaOffset} or
 * {@code consumerGroup} has no validation or behavior worth attaching
 * beyond what a plain {@code long}/{@code String} already gives.
 */
public record LifecycleEvent(
    String eventType,
    String roomId,
    String messageId,
    int kafkaPartition,
    long kafkaOffset,
    String consumerGroup,
    Instant occurredAt) {}
