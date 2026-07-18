package com.kafkaddd.chat.chatdelivery.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Reference to a user owned by the Identity bounded context. Chat Delivery
 * never imports Identity's domain types — it only ever holds this opaque
 * identifier (PLAN_SSDLC.md §9's bounded-context separation).
 */
public record UserId(UUID value) {

  public UserId {
    Objects.requireNonNull(value, "value");
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
