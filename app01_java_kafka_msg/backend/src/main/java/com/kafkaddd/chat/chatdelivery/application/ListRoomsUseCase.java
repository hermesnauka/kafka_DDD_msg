package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Lists the rooms a user is a participant of (PLAN_SSDLC.md §7.1
 * {@code GET /api/v1/rooms}, FR-5/SR-5 — never another user's rooms).
 *
 * <p>Loads each room's full aggregate to build its summary; fine for a
 * scaffold, but N+1 for a user in many rooms. A dedicated read-model query
 * (participant count/last-message time without loading every message)
 * would be the right fix if this ever shows up as a bottleneck.
 */
@Service
public class ListRoomsUseCase {

  private final ChatRoomRepository chatRoomRepository;

  public ListRoomsUseCase(ChatRoomRepository chatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
  }

  public List<RoomSummary> listForUser(UserId userId) {
    List<RoomId> roomIds = chatRoomRepository.findRoomIdsForParticipant(userId);
    return roomIds.stream()
        .map(chatRoomRepository::findById)
        .flatMap(Optional::stream)
        .map(RoomSummary::from)
        .toList();
  }
}
