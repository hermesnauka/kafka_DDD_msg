package com.kafkaddd.chat.chatdelivery.infrastructure;

import com.kafkaddd.chat.chatdelivery.domain.ChatRoom;
import com.kafkaddd.chat.chatdelivery.domain.ChatRoomRepository;
import com.kafkaddd.chat.chatdelivery.domain.Message;
import com.kafkaddd.chat.chatdelivery.domain.MessageContent;
import com.kafkaddd.chat.chatdelivery.domain.MessageId;
import com.kafkaddd.chat.chatdelivery.domain.MessageSnapshot;
import com.kafkaddd.chat.chatdelivery.domain.MessageStatus;
import com.kafkaddd.chat.chatdelivery.domain.Participant;
import com.kafkaddd.chat.chatdelivery.domain.ParticipantSnapshot;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.Timestamp;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter implementing the domain's {@link ChatRoomRepository} port. Three
 * tables back one aggregate: {@code chat_rooms} (existence anchor),
 * {@code chat_room_participants}, {@code chat_messages}.
 *
 * <p>Participants have no domain-assigned ID of their own (unlike
 * {@code Message}, which does), and the domain model has no
 * "add a participant to an existing room" operation yet — only
 * {@code ChatRoom.create} establishes them. So participants are inserted
 * only on a room's first save; re-saving an existing room never touches
 * {@code chat_room_participants}. Revisit this the moment a
 * post-creation participant-management feature is added.
 */
@Repository
class ChatRoomRepositoryAdapter implements ChatRoomRepository {

  private final ChatRoomJpaRepository roomRepository;
  private final ParticipantJpaRepository participantRepository;
  private final MessageJpaRepository messageRepository;

  ChatRoomRepositoryAdapter(
      ChatRoomJpaRepository roomRepository,
      ParticipantJpaRepository participantRepository,
      MessageJpaRepository messageRepository) {
    this.roomRepository = roomRepository;
    this.participantRepository = participantRepository;
    this.messageRepository = messageRepository;
  }

  @Override
  public Optional<ChatRoom> findById(RoomId id) {
    if (roomRepository.findById(id.value()).isEmpty()) {
      return Optional.empty();
    }
    List<ParticipantSnapshot> participants =
        participantRepository.findByRoomId(id.value()).stream().map(this::toParticipantSnapshot).toList();
    List<MessageSnapshot> messages =
        messageRepository.findByRoomIdOrderBySentAtAsc(id.value()).stream().map(this::toMessageSnapshot).toList();
    return Optional.of(ChatRoom.reconstitute(id, participants, messages));
  }

  @Override
  @Transactional
  public ChatRoom save(ChatRoom room) {
    UUID roomId = room.id().value();
    boolean isNewRoom = roomRepository.findById(roomId).isEmpty();
    if (isNewRoom) {
      roomRepository.save(new ChatRoomEntity(roomId));
      participantRepository.saveAll(room.participants().stream().map(p -> toParticipantEntity(roomId, p)).toList());
    }
    messageRepository.saveAll(room.messages().stream().map(m -> toMessageEntity(roomId, m)).toList());
    return room;
  }

  @Override
  public List<RoomId> findRoomIdsForParticipant(UserId userId) {
    return participantRepository.findByUserId(userId.value()).stream()
        .map(p -> new RoomId(p.getRoomId()))
        .distinct()
        .toList();
  }

  private ParticipantSnapshot toParticipantSnapshot(ParticipantEntity entity) {
    return new ParticipantSnapshot(new UserId(entity.getUserId()), new Timestamp(entity.getJoinedAt()));
  }

  private MessageSnapshot toMessageSnapshot(MessageEntity entity) {
    return new MessageSnapshot(
        new MessageId(entity.getId()),
        new UserId(entity.getSenderId()),
        new MessageContent(entity.getContent()),
        new Timestamp(entity.getSentAt()),
        toDomainStatus(entity.getStatus()));
  }

  private ParticipantEntity toParticipantEntity(UUID roomId, Participant participant) {
    return new ParticipantEntity(UUID.randomUUID(), roomId, participant.userId().value(), participant.joinedAt().value());
  }

  private MessageEntity toMessageEntity(UUID roomId, Message message) {
    return new MessageEntity(
        message.id().value(),
        roomId,
        message.senderId().value(),
        message.content().value(),
        message.sentAt().value(),
        toEntityStatus(message.status()));
  }

  private MessageStatus toDomainStatus(MessageEntity.Status status) {
    return MessageStatus.valueOf(status.name());
  }

  private MessageEntity.Status toEntityStatus(MessageStatus status) {
    return MessageEntity.Status.valueOf(status.name());
  }
}
