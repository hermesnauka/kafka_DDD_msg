package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.Timestamp;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Creates a direct or group chat room (PLAN_SSDLC.md §7.1 {@code POST /api/v1/rooms}). */
@Service
public class CreateRoomUseCase {

  private final ChatRoomRepository chatRoomRepository;

  public CreateRoomUseCase(ChatRoomRepository chatRoomRepository) {
    this.chatRoomRepository = chatRoomRepository;
  }

  /** {@code creatorId} is always a participant, whether or not the caller also listed it. */
  public RoomSummary create(UserId creatorId, List<UserId> otherParticipantIds) {
    Set<UserId> participants = new LinkedHashSet<>();
    participants.add(creatorId);
    participants.addAll(otherParticipantIds);

    ChatRoom room = ChatRoom.create(RoomId.newId(), participants, Timestamp.now());
    chatRoomRepository.save(room);
    return RoomSummary.from(room);
  }
}
