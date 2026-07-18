package com.kafkaddd.chat.chatdelivery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.MessageStatus;
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
class SendMessageUseCaseTest {

  @Mock private ChatRoomRepository chatRoomRepository;
  @Mock private DomainEventPublisher eventPublisher;

  private SendMessageUseCase useCase() {
    return new SendMessageUseCase(chatRoomRepository, eventPublisher);
  }

  @Test
  void sendingAsAParticipantPersistsAndPublishesMessageSent() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    ChatRoom room = ChatRoom.create(roomId, List.of(alice), Timestamp.now());
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

    MessageView view = useCase().send(roomId, alice, "hi");

    assertThat(view.status()).isEqualTo(MessageStatus.PENDING.name());
    assertThat(view.content()).isEqualTo("hi");
    verify(chatRoomRepository, times(1)).save(room);
    verify(eventPublisher, times(1)).publish(any());
  }

  @Test
  void sendingToAnUnknownRoomIsRejectedWithoutPersistingOrPublishing() {
    RoomId roomId = RoomId.newId();
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase().send(roomId, new UserId(UUID.randomUUID()), "hi"))
        .isInstanceOf(NoSuchRoomException.class);
    verify(chatRoomRepository, never()).save(any());
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void sendingAsANonParticipantIsRejectedWithoutPersistingOrPublishing() {
    UserId alice = new UserId(UUID.randomUUID());
    UserId outsider = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    ChatRoom room = ChatRoom.create(roomId, List.of(alice), Timestamp.now());
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

    assertThatThrownBy(() -> useCase().send(roomId, outsider, "hi")).isInstanceOf(NotAParticipantException.class);
    verify(chatRoomRepository, never()).save(any());
    verify(eventPublisher, never()).publish(any());
  }
}
