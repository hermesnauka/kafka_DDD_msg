package com.kafkaddd.chat.chatdelivery.domain;

import java.util.Objects;
import java.util.UUID;

/** Identity of a {@link ChatRoom} aggregate. */
public record RoomId(UUID value) {

  public RoomId {
    Objects.requireNonNull(value, "value");
  }

  public static RoomId newId() {
    return new RoomId(UUID.randomUUID());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
