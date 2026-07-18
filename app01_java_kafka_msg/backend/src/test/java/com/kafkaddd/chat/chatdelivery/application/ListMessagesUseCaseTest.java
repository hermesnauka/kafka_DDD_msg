package com.kafkaddd.chat.chatdelivery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.MessageContent;
import com.kafkaddd.chat.chatdelivery.domain.NoSuchRoomException;
import com.kafkaddd.chat.chatdelivery.domain.NotAParticipantException;
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
class ListMessagesUseCaseTest {

  @Mock private ChatRoomRepository chatRoomRepository;

  private ListMessagesUseCase useCase() {
    return new ListMessagesUseCase(chatRoomRepository);
  }

  @Test
  void aParticipantSeesTheRoomsMessages() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    ChatRoom room = ChatRoom.create(roomId, List.of(alice), Timestamp.now());
    room.sendMessage(alice, new MessageContent("hi"), Timestamp.now());
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

    List<MessageView> messages = useCase().list(roomId, alice);

    assertThat(messages).hasSize(1);
    assertThat(messages.get(0).content()).isEqualTo("hi");
  }

  @Test
  void anUnknownRoomIsRejected() {
    RoomId roomId = RoomId.newId();
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase().list(roomId, new UserId(UUID.randomUUID())))
        .isInstanceOf(NoSuchRoomException.class);
  }

  @Test
  void aNonParticipantIsRejected() {
    UserId alice = new UserId(UUID.randomUUID());
    UserId outsider = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    ChatRoom room = ChatRoom.create(roomId, List.of(alice), Timestamp.now());
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

    assertThatThrownBy(() -> useCase().list(roomId, outsider)).isInstanceOf(NotAParticipantException.class);
  }
}
