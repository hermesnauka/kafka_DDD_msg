package com.kafkaddd.chat.chatdelivery.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for {@link ChatRoom}. Expressed purely in domain types —
 * no Spring/JPA here (NFR-Arch-1). The real adapter (Spring Data JPA-backed)
 * lives in {@code chatdelivery.infrastructure}.
 */
public interface ChatRoomRepository {

  Optional<ChatRoom> findById(RoomId id);

  ChatRoom save(ChatRoom room);

  /** Room IDs {@code userId} is a participant of (FR-5/SR-5's "list only my rooms"). */
  List<RoomId> findRoomIdsForParticipant(UserId userId);
}
