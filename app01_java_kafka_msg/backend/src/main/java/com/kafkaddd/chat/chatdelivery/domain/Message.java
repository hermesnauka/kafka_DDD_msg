package com.kafkaddd.chat.chatdelivery.domain;

import java.util.Objects;

/**
 * A single chat message inside a {@link ChatRoom}. Only the aggregate root
 * constructs and transitions a {@code Message} — the constructor and state
 * transition methods are package-private so no other class can put a
 * {@code Message} in an inconsistent state.
 */
public final class Message {

  private final MessageId id;
  private final UserId senderId;
  private final MessageContent content;
  private final Timestamp sentAt;
  private MessageStatus status;

  Message(MessageId id, UserId senderId, MessageContent content, Timestamp sentAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.senderId = Objects.requireNonNull(senderId, "senderId");
    this.content = Objects.requireNonNull(content, "content");
    this.sentAt = Objects.requireNonNull(sentAt, "sentAt");
    this.status = MessageStatus.PENDING;
  }

  /**
   * Rebuilds a {@code Message} with an already-persisted status, bypassing
   * the {@code markPolled}/{@code markDelivered} transition checks — used
   * only by {@link ChatRoom#reconstitute} when loading from storage, never
   * for an actual state transition.
   */
  static Message reconstitute(
      MessageId id, UserId senderId, MessageContent content, Timestamp sentAt, MessageStatus status) {
    Message message = new Message(id, senderId, content, sentAt);
    message.status = status;
    return message;
  }

  void markPolled() {
    requireStatus(MessageStatus.PENDING);
    this.status = MessageStatus.POLLED;
  }

  void markDelivered() {
    requireStatus(MessageStatus.POLLED);
    this.status = MessageStatus.DELIVERED;
  }

  private void requireStatus(MessageStatus expected) {
    if (status != expected) {
      throw new IllegalMessageStateException(id, status, expected);
    }
  }

  public MessageId id() {
    return id;
  }

  public UserId senderId() {
    return senderId;
  }

  public MessageContent content() {
    return content;
  }

  public Timestamp sentAt() {
    return sentAt;
  }

  public MessageStatus status() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Message other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
