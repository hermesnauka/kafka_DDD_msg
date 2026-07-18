package com.kafkaddd.chat.chatdelivery.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_room_participants")
class ParticipantEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private UUID roomId;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private Instant joinedAt;

  protected ParticipantEntity() {
    // required by JPA
  }

  ParticipantEntity(UUID id, UUID roomId, UUID userId, Instant joinedAt) {
    this.id = id;
    this.roomId = roomId;
    this.userId = userId;
    this.joinedAt = joinedAt;
  }

  UUID getRoomId() {
    return roomId;
  }

  UUID getUserId() {
    return userId;
  }

  Instant getJoinedAt() {
    return joinedAt;
  }
}
