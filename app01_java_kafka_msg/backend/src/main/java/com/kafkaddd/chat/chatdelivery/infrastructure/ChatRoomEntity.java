package com.kafkaddd.chat.chatdelivery.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Persistence anchor for a {@link com.kafkaddd.chat.chatdelivery.domain.ChatRoom} — its participants and messages live in their own tables (see {@link ParticipantEntity}, {@link MessageEntity}). */
@Entity
@Table(name = "chat_rooms")
class ChatRoomEntity {

  @Id private UUID id;

  protected ChatRoomEntity() {
    // required by JPA
  }

  ChatRoomEntity(UUID id) {
    this.id = id;
  }

  UUID getId() {
    return id;
  }
}
