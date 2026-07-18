package com.kafkaddd.chat.chatdelivery.domain;

/** Recorded when a consumer instance picks up the message ({@code POLLED}). */
public record MessagePolled(RoomId roomId, MessageId messageId, Timestamp occurredAt) implements DomainEvent {}
