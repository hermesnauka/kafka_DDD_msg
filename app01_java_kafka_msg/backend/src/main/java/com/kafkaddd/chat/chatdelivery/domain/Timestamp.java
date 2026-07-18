package com.kafkaddd.chat.chatdelivery.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/** A point in time attached to a domain event or entity state change. */
public record Timestamp(Instant value) {

  public Timestamp {
    Objects.requireNonNull(value, "value");
  }

  public static Timestamp now() {
    return new Timestamp(Instant.now());
  }

  public static Timestamp now(Clock clock) {
    return new Timestamp(Instant.now(clock));
  }

  public boolean isBefore(Timestamp other) {
    return value.isBefore(other.value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
