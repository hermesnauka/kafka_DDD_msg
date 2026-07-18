package com.kafkaddd.chat.chatdelivery.infrastructure;

import com.kafkaddd.chat.chatdelivery.domain.DomainEvent;
import com.kafkaddd.chat.chatdelivery.domain.MessageDelivered;
import com.kafkaddd.chat.chatdelivery.domain.MessagePolled;
import com.kafkaddd.chat.chatdelivery.domain.MessageSent;
import java.time.Instant;

/**
 * Wire schema for {@code chat.room.events} (PLAN_SSDLC.md §7.3). {@code
 * senderId}/{@code content} are only ever populated for {@code
 * "MessageSent"} — {@code MessagePolled}/{@code MessageDelivered} carry
 * metadata only, never the message body, matching the plan's information-
 * disclosure boundary for this topic.
 */
record ChatEventEnvelope(
    String eventType, String roomId, String messageId, String senderId, String content, Instant occurredAt) {

  static ChatEventEnvelope from(DomainEvent event) {
    return switch (event) {
      case MessageSent e ->
          new ChatEventEnvelope(
              "MessageSent",
              e.roomId().value().toString(),
              e.messageId().value().toString(),
              e.senderId().value().toString(),
              e.content().value(),
              e.occurredAt().value());
      case MessagePolled e ->
          new ChatEventEnvelope(
              "MessagePolled", e.roomId().value().toString(), e.messageId().value().toString(), null, null, e.occurredAt().value());
      case MessageDelivered e ->
          new ChatEventEnvelope(
              "MessageDelivered", e.roomId().value().toString(), e.messageId().value().toString(), null, null, e.occurredAt().value());
    };
  }
}
