package com.kafkaddd.chat.chatdelivery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateRoomUseCaseTest {

  @Mock private ChatRoomRepository chatRoomRepository;
  @Captor private ArgumentCaptor<ChatRoom> roomCaptor;

  @Test
  void createsARoomWithTheCreatorAndEveryOtherParticipant() {
    UserId creator = new UserId(UUID.randomUUID());
    UserId bob = new UserId(UUID.randomUUID());
    when(chatRoomRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    RoomSummary summary = new CreateRoomUseCase(chatRoomRepository).create(creator, List.of(bob));

    verify(chatRoomRepository, times(1)).save(roomCaptor.capture());
    ChatRoom saved = roomCaptor.getValue();
    assertThat(saved.isParticipant(creator)).isTrue();
    assertThat(saved.isParticipant(bob)).isTrue();
    assertThat(summary.participantIds()).hasSize(2);
  }

  @Test
  void doesNotDuplicateTheCreatorIfAlsoListedAsAnOtherParticipant() {
    UserId creator = new UserId(UUID.randomUUID());
    when(chatRoomRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    RoomSummary summary = new CreateRoomUseCase(chatRoomRepository).create(creator, List.of(creator));

    assertThat(summary.participantIds()).hasSize(1);
  }
}
