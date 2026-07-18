package com.kafkaddd.chat.chatdelivery.domain;

/**
 * Thrown when a user who is not a participant of a {@link ChatRoom}
 * attempts an action scoped to that room. This is the domain-level half of
 * SR-5/FR-5's authorization requirement; the infrastructure layer must
 * still re-check membership on every read/list/subscribe path (AGENTS.md) —
 * this exception guards the aggregate's own invariants, it is not a
 * substitute for that check.
 */
public class NotAParticipantException extends RuntimeException {

  public NotAParticipantException(RoomId roomId, UserId userId) {
    super("user " + userId + " is not a participant of room " + roomId);
  }
}
