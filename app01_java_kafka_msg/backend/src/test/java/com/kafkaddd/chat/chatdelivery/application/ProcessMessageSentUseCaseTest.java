package com.kafkaddd.chat.chatdelivery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.Message;
import com.kafkaddd.chat.chatdelivery.domain.MessageContent;
import com.kafkaddd.chat.chatdelivery.domain.MessageId;
import com.kafkaddd.chat.chatdelivery.domain.MessageStatus;
import com.kafkaddd.chat.chatdelivery.domain.NoSuchMessageException;
import com.kafkaddd.chat.chatdelivery.domain.NoSuchRoomException;
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
class ProcessMessageSentUseCaseTest {

  @Mock private ChatRoomRepository chatRoomRepository;
  @Mock private DomainEventPublisher eventPublisher;
  @Mock private MessageDeliveryNotifier notifier;

  private ProcessMessageSentUseCase useCase() {
    return new ProcessMessageSentUseCase(chatRoomRepository, eventPublisher, notifier);
  }

  @Test
  void aPendingMessageIsDrivenToDeliveredAndTheClientIsNotified() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    ChatRoom room = ChatRoom.create(roomId, List.of(alice), Timestamp.now());
    Message message = room.sendMessage(alice, new MessageContent("hi"), Timestamp.now());
    room.pullDomainEvents(); // drop MessageSent, as SendMessageUseCase would already have
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

    useCase().process(roomId, message.id());

    assertThat(message.status()).isEqualTo(MessageStatus.DELIVERED);
    verify(chatRoomRepository, times(2)).save(room); // once for POLLED, once for DELIVERED
    verify(eventPublisher, times(2)).publish(any()); // MessagePolled, MessageDelivered
    verify(notifier, times(1)).notifyDelivered(roomId, message);
  }

  @Test
  void aRedeliveredEventForAnAlreadyProcessedMessageIsANoOp() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    ChatRoom room = ChatRoom.create(roomId, List.of(alice), Timestamp.now());
    Message message = room.sendMessage(alice, new MessageContent("hi"), Timestamp.now());
    room.markPolled(message.id(), Timestamp.now());
    room.markDelivered(message.id(), Timestamp.now());
    room.pullDomainEvents();
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

    useCase().process(roomId, message.id());

    verify(chatRoomRepository, never()).save(any());
    verifyNoInteractions(eventPublisher, notifier);
  }

  @Test
  void anUnknownRoomIsRejected() {
    RoomId roomId = RoomId.newId();
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase().process(roomId, MessageId.newId())).isInstanceOf(NoSuchRoomException.class);
  }

  @Test
  void anUnknownMessageWithinAKnownRoomIsRejected() {
    UserId alice = new UserId(UUID.randomUUID());
    RoomId roomId = RoomId.newId();
    ChatRoom room = ChatRoom.create(roomId, List.of(alice), Timestamp.now());
    when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));

    assertThatThrownBy(() -> useCase().process(roomId, MessageId.newId()))
        .isInstanceOf(NoSuchMessageException.class);
  }
}
