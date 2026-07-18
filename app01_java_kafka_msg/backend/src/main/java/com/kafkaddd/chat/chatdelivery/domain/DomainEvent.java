package com.kafkaddd.chat.chatdelivery.domain;

/**
 * Marker for events recorded by {@link ChatRoom} and drained by the
 * application layer for publishing to {@code chat.room.events}
 * (PLAN_SSDLC.md §7.3/§7.4). Payloads here carry only metadata needed for
 * that topic contract — never the message body for anything but the
 * initial send (see {@link MessageSent}).
 */
public sealed interface DomainEvent permits MessageSent, MessagePolled, MessageDelivered {
  RoomId roomId();

  MessageId messageId();

  Timestamp occurredAt();
}
