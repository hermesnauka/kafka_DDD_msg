package com.kafkaddd.chat.identity.domain;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A point in time within the Identity context. Deliberately a separate
 * type from {@code chatdelivery.domain.Timestamp} — see this package's
 * {@code package-info.java} on bounded-context separation.
 */
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

  public Timestamp plus(Duration duration) {
    return new Timestamp(value.plus(duration));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
