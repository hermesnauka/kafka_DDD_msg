package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.NoSuchRoomException;
import com.kafkaddd.chat.chatdelivery.domain.NotAParticipantException;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Lists a room's message history (PLAN_SSDLC.md §7.1
 * {@code GET /api/v1/rooms/{roomId}/messages}). Room membership (SR-5) is
 * re-checked here explicitly — unlike {@link SendMessageUseCase}, a read
 * path has no domain method of its own to lean on for this, so the check
 * has to live at this boundary.
 */
@Service
public class ListMessagesUseCase {

  private final ChatRoomRepository chatRoomRepository;

  public ListMessagesUseCase(ChatRoomRepository chatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
  }

  public List<MessageView> list(RoomId roomId, UserId requesterId) {
    ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new NoSuchRoomException(roomId));
    if (!room.isParticipant(requesterId)) {
      throw new NotAParticipantException(roomId, requesterId);
    }
    return room.messages().stream().map(MessageView::from).toList();
  }
}
