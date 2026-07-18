package com.kafkaddd.chat.chatdelivery.domain;

import java.util.Objects;

/** A user who belongs to a {@link ChatRoom}. Equality is by {@link UserId}. */
public final class Participant {

  private final UserId userId;
  private final Timestamp joinedAt;

  Participant(UserId userId, Timestamp joinedAt) {
    this.userId = Objects.requireNonNull(userId, "userId");
    this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt");
  }

  public UserId userId() {
    return userId;
  }

  public Timestamp joinedAt() {
    return joinedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Participant other)) return false;
    return userId.equals(other.userId);
  }

  @Override
  public int hashCode() {
    return userId.hashCode();
  }
}
