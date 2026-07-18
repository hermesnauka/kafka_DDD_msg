package com.kafkaddd.chat.identity.domain;

import java.util.Objects;
import java.util.UUID;

/** Identity of a {@link User} aggregate, owned by the Identity bounded context. */
public record UserId(UUID value) {

  public UserId {
    Objects.requireNonNull(value, "value");
  }

  public static UserId newId() {
    return new UserId(UUID.randomUUID());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
