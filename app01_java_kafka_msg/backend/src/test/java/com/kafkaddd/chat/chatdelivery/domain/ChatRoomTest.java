package com.kafkaddd.chat.chatdelivery.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Domain-layer tests: no Spring context, no Kafka, no database — just the
 * aggregate's own invariants (NFR-Arch-1). If this test needed
 * {@code @SpringBootTest} to run, that would itself be a layering
 * violation (AGENTS.md).
 */
class ChatRoomTest {

  private static final Timestamp NOW = Timestamp.now();

  @Test
  void sendMessageByAParticipantStartsPending() {
    UserId alice = new UserId(java.util.UUID.randomUUID());
    UserId bob = new UserId(java.util.UUID.randomUUID());
    ChatRoom room = ChatRoom.create(RoomId.newId(), List.of(alice, bob), NOW);

    Message message = room.sendMessage(alice, new MessageContent("hi bob"), NOW);

    assertThat(message.status()).isEqualTo(MessageStatus.PENDING);
    assertThat(message.senderId()).isEqualTo(alice);
    assertThat(room.messages()).containsExactly(message);
  }

  @Test
  void sendMessageByANonParticipantIsRejected() {
    UserId alice = new UserId(java.util.UUID.randomUUID());
    UserId outsider = new UserId(java.util.UUID.randomUUID());
    ChatRoom room = ChatRoom.create(RoomId.newId(), List.of(alice), NOW);

    assertThatThrownBy(() -> room.sendMessage(outsider, new MessageContent("hi"), NOW))
        .isInstanceOf(NotAParticipantException.class);
    assertThat(room.messages()).isEmpty();
  }

  @Test
  void fullLifecycleTransitionsPendingToPolledToDeliveredAndRecordsEventsInOrder() {
    UserId alice = new UserId(java.util.UUID.randomUUID());
    ChatRoom room = ChatRoom.create(RoomId.newId(), List.of(alice), NOW);

    Message message = room.sendMessage(alice, new MessageContent("hi"), NOW);
    room.markPolled(message.id(), NOW);
    room.markDelivered(message.id(), NOW);

    assertThat(message.status()).isEqualTo(MessageStatus.DELIVERED);

    List<DomainEvent> events = room.pullDomainEvents();
    assertThat(events).hasSize(3);
    assertThat(events.get(0)).isInstanceOf(MessageSent.class);
    assertThat(events.get(1)).isInstanceOf(MessagePolled.class);
    assertThat(events.get(2)).isInstanceOf(MessageDelivered.class);
    assertThat(events).allSatisfy(e -> assertThat(e.messageId()).isEqualTo(message.id()));
  }

  @Test
  void pullDomainEventsClearsThemSoTheyArentPublishedTwice() {
    UserId alice = new UserId(java.util.UUID.randomUUID());
    ChatRoom room = ChatRoom.create(RoomId.newId(), List.of(alice), NOW);
    room.sendMessage(alice, new MessageContent("hi"), NOW);

    assertThat(room.pullDomainEvents()).hasSize(1);
    assertThat(room.pullDomainEvents()).isEmpty();
  }

  @Test
  void markingPolledTwiceIsRejected() {
    UserId alice = new UserId(java.util.UUID.randomUUID());
    ChatRoom room = ChatRoom.create(RoomId.newId(), List.of(alice), NOW);
    Message message = room.sendMessage(alice, new MessageContent("hi"), NOW);
    room.markPolled(message.id(), NOW);

    assertThatThrownBy(() -> room.markPolled(message.id(), NOW))
        .isInstanceOf(IllegalMessageStateException.class);
  }

  @Test
  void markingDeliveredBeforePolledIsRejected() {
    UserId alice = new UserId(java.util.UUID.randomUUID());
    ChatRoom room = ChatRoom.create(RoomId.newId(), List.of(alice), NOW);
    Message message = room.sendMessage(alice, new MessageContent("hi"), NOW);

    assertThatThrownBy(() -> room.markDelivered(message.id(), NOW))
        .isInstanceOf(IllegalMessageStateException.class);
  }

  @Test
  void transitioningAnUnknownMessageIsRejected() {
    UserId alice = new UserId(java.util.UUID.randomUUID());
    ChatRoom room = ChatRoom.create(RoomId.newId(), List.of(alice), NOW);

    assertThatThrownBy(() -> room.markPolled(MessageId.newId(), NOW))
        .isInstanceOf(NoSuchMessageException.class);
  }

  @Test
  void creatingARoomWithNoParticipantsIsRejected() {
    assertThatThrownBy(() -> ChatRoom.create(RoomId.newId(), List.of(), NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void messageContentRejectsBlankAndOversizedText() {
    assertThatThrownBy(() -> new MessageContent(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new MessageContent("x".repeat(MessageContent.MAX_LENGTH + 1)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
