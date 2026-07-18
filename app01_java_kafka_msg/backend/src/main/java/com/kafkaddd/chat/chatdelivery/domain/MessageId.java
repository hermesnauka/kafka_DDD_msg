package com.kafkaddd.chat.chatdelivery.domain;

import java.util.Objects;
import java.util.UUID;

/** Identity of a {@link Message} entity within a {@link ChatRoom}. */
public record MessageId(UUID value) {

  public MessageId {
    Objects.requireNonNull(value, "value");
  }

  public static MessageId newId() {
    return new MessageId(UUID.randomUUID());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
