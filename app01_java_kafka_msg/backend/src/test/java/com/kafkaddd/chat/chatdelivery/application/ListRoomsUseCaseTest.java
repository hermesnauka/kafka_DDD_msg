package com.kafkaddd.chat.chatdelivery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.Timestamp;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListRoomsUseCaseTest {

  @Mock private ChatRoomRepository chatRoomRepository;

  @Test
  void listsEveryRoomTheUserIsAParticipantOf() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId room1 = RoomId.newId();
    RoomId room2 = RoomId.newId();
    when(chatRoomRepository.findRoomIdsForParticipant(alice)).thenReturn(List.of(room1, room2));
    when(chatRoomRepository.findById(room1))
        .thenReturn(Optional.of(ChatRoom.create(room1, List.of(alice), Timestamp.now())));
    when(chatRoomRepository.findById(room2))
        .thenReturn(Optional.of(ChatRoom.create(room2, List.of(alice), Timestamp.now())));

    List<RoomSummary> summaries = new ListRoomsUseCase(chatRoomRepository).listForUser(alice);

    assertThat(summaries).hasSize(2);
    assertThat(summaries).extracting(RoomSummary::id).containsExactlyInAnyOrder(room1.toString(), room2.toString());
  }

  @Test
  void silentlySkipsARoomIdThatNoLongerResolves() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId ghostRoom = RoomId.newId();
    when(chatRoomRepository.findRoomIdsForParticipant(alice)).thenReturn(List.of(ghostRoom));
    when(chatRoomRepository.findById(ghostRoom)).thenReturn(Optional.empty());

    List<RoomSummary> summaries = new ListRoomsUseCase(chatRoomRepository).listForUser(alice);

    assertThat(summaries).isEmpty();
  }
}
