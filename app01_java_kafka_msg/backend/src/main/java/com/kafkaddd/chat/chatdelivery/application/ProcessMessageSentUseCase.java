package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.Message;
import com.kafkaddd.chat.chatdelivery.domain.MessageId;
import com.kafkaddd.chat.chatdelivery.domain.MessageStatus;
import com.kafkaddd.chat.chatdelivery.domain.NoSuchMessageException;
import com.kafkaddd.chat.chatdelivery.domain.NoSuchRoomException;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.Timestamp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reacts to a {@code MessageSent} event read off {@code chat.room.events}:
 * this is the "consumer group picks it up" step from PLAN_SSDLC.md's
 * educational narrative. Transitions PENDING -&gt; POLLED -&gt; DELIVERED,
 * publishing {@code MessagePolled}/{@code MessageDelivered} back onto the
 * same topic, then pushes the delivered message over STOMP.
 *
 * <p>Kafka is at-least-once: the same {@code MessageSent} event can be
 * redelivered (e.g. after a consumer restart before its offset commits).
 * {@link #process} is idempotent against that — if the message is no
 * longer PENDING, it's already been handled, and this is a no-op.
 */
@Service
public class ProcessMessageSentUseCase {

  private final ChatRoomRepository chatRoomRepository;
  private final DomainEventPublisher eventPublisher;
  private final MessageDeliveryNotifier notifier;

  public ProcessMessageSentUseCase(
      ChatRoomRepository chatRoomRepository, DomainEventPublisher eventPublisher, MessageDeliveryNotifier notifier) {
    this.chatRoomRepository = chatRoomRepository;
    this.eventPublisher = eventPublisher;
    this.notifier = notifier;
  }

  @Transactional
  public void process(RoomId roomId, MessageId messageId) {
    ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new NoSuchRoomException(roomId));
    Message message =
        room.messages().stream()
            .filter(m -> m.id().equals(messageId))
            .findFirst()
            .orElseThrow(() -> new NoSuchMessageException(roomId, messageId));

    if (message.status() != MessageStatus.PENDING) {
      return; // already processed — a Kafka redelivery, not a new message
    }

    Timestamp now = Timestamp.now();

    room.markPolled(messageId, now);
    chatRoomRepository.save(room);
    room.pullDomainEvents().forEach(eventPublisher::publish);

    room.markDelivered(messageId, now);
    chatRoomRepository.save(room);
    room.pullDomainEvents().forEach(eventPublisher::publish);

    notifier.notifyDelivered(roomId, message);
  }
}
