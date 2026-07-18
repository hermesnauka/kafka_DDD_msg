package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.Participant;
import java.util.List;

/** Read-only projection of a {@link ChatRoom} for the room-list view (FR-5/SR-5). */
public record RoomSummary(String id, List<String> participantIds, int messageCount) {

  public static RoomSummary from(ChatRoom room) {
    List<String> participantIds = room.participants().stream().map(Participant::userId).map(Object::toString).toList();
    return new RoomSummary(room.id().toString(), participantIds, room.messages().size());
  }
}
