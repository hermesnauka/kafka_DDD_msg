package com.kafkaddd.chat.chatdelivery.domain;

/** Recorded when the message is pushed to the recipient's UI ({@code DELIVERED}). */
public record MessageDelivered(RoomId roomId, MessageId messageId, Timestamp occurredAt) implements DomainEvent {}
