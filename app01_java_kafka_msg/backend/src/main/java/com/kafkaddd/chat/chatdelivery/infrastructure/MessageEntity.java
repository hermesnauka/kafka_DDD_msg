package com.kafkaddd.chat.chatdelivery.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
class MessageEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private UUID roomId;

  @Column(nullable = false)
  private UUID senderId;

  @Column(nullable = false, columnDefinition = "text")
  private String content;

  @Column(nullable = false)
  private Instant sentAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  protected MessageEntity() {
    // required by JPA
  }

  MessageEntity(UUID id, UUID roomId, UUID senderId, String content, Instant sentAt, Status status) {
    this.id = id;
    this.roomId = roomId;
    this.senderId = senderId;
    this.content = content;
    this.sentAt = sentAt;
    this.status = status;
  }

  UUID getId() {
    return id;
  }

  UUID getRoomId() {
    return roomId;
  }

  UUID getSenderId() {
    return senderId;
  }

  String getContent() {
    return content;
  }

  Instant getSentAt() {
    return sentAt;
  }

  Status getStatus() {
    return status;
  }

  void setStatus(Status status) {
    this.status = status;
  }

  /** Mirrors {@code com.kafkaddd.chat.chatdelivery.domain.MessageStatus} — kept separate so the domain enum has no JPA annotations on it. */
  enum Status {
    PENDING,
    POLLED,
    DELIVERED
  }
}
