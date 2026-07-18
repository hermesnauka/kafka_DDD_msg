package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.Message;
import com.kafkaddd.chat.chatdelivery.domain.MessageContent;
import com.kafkaddd.chat.chatdelivery.domain.NoSuchRoomException;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.Timestamp;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends a message (PLAN_SSDLC.md §7.1 {@code POST /api/v1/rooms/{roomId}/messages}).
 * Room-membership (SR-5) is enforced by {@link ChatRoom#sendMessage}
 * itself, which throws {@code NotAParticipantException} for a
 * non-participant — this use case doesn't duplicate that check.
 */
@Service
public class SendMessageUseCase {

  private final ChatRoomRepository chatRoomRepository;
  private final DomainEventPublisher eventPublisher;

  public SendMessageUseCase(ChatRoomRepository chatRoomRepository, DomainEventPublisher eventPublisher) {
    this.chatRoomRepository = chatRoomRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public MessageView send(RoomId roomId, UserId senderId, String content) {
    ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new NoSuchRoomException(roomId));

    Message message = room.sendMessage(senderId, new MessageContent(content), Timestamp.now());
    chatRoomRepository.save(room);
    // Publishes MessageSent; the Kafka consumer (ProcessMessageSentUseCase)
    // picks it up asynchronously and drives POLLED -> DELIVERED.
    room.pullDomainEvents().forEach(eventPublisher::publish);

    return MessageView.from(message);
  }
}
