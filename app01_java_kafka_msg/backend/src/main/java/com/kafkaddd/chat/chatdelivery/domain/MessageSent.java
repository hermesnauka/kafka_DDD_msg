package com.kafkaddd.chat.chatdelivery.domain;

/**
 * Recorded when a {@link Message} is first created ({@code PENDING}). The
 * only event of the three that carries the message body downstream to the
 * delivery consumer — {@link MessagePolled} and {@link MessageDelivered}
 * never do (PLAN_SSDLC.md §7.3).
 */
public record MessageSent(
    RoomId roomId, MessageId messageId, UserId senderId, MessageContent content, Timestamp occurredAt)
    implements DomainEvent {}
