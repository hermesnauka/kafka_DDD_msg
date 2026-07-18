package com.kafkaddd.chat.chatdelivery.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate root for a chat room (direct or group). Owns the participant
 * list and every {@link Message} sent in the room, and is the only class
 * allowed to create a {@code Message} or transition its status — all
 * mutation goes through here so the room's invariants (only participants
 * send/receive; messages transition PENDING -&gt; POLLED -&gt; DELIVERED in
 * order) can never be bypassed (PLAN_SSDLC.md §5.2, §7.4).
 */
public final class ChatRoom {

  private final RoomId id;
  private final Map<UserId, Participant> participants = new LinkedHashMap<>();
  private final Map<MessageId, Message> messages = new LinkedHashMap<>();
  private final List<DomainEvent> domainEvents = new ArrayList<>();

  private ChatRoom(RoomId id) {
    this.id = Objects.requireNonNull(id, "id");
  }

  /** Creates a new room with its initial participants (at least one). */
  public static ChatRoom create(RoomId id, Collection<UserId> initialParticipants, Timestamp now) {
    if (initialParticipants == null || initialParticipants.isEmpty()) {
      throw new IllegalArgumentException("a chat room needs at least one participant");
    }
    ChatRoom room = new ChatRoom(id);
    for (UserId userId : initialParticipants) {
      room.participants.put(userId, new Participant(userId, now));
    }
    return room;
  }

  public RoomId id() {
    return id;
  }

  public boolean isParticipant(UserId userId) {
    return participants.containsKey(userId);
  }

  public Collection<Participant> participants() {
    return Collections.unmodifiableCollection(participants.values());
  }

  public Collection<Message> messages() {
    return Collections.unmodifiableCollection(messages.values());
  }

  /**
   * Sends a message from {@code senderId}. Records a {@link MessageSent}
   * event. Throws {@link NotAParticipantException} if the sender isn't a
   * participant (SR-5's domain-level half — infrastructure must still
   * re-check this on every access path).
   */
  public Message sendMessage(UserId senderId, MessageContent content, Timestamp now) {
    requireParticipant(senderId);
    MessageId messageId = MessageId.newId();
    Message message = new Message(messageId, senderId, content, now);
    messages.put(messageId, message);
    domainEvents.add(new MessageSent(id, messageId, senderId, content, now));
    return message;
  }

  /** Marks a message as picked up by a consumer. Records {@link MessagePolled}. */
  public void markPolled(MessageId messageId, Timestamp now) {
    messageOrThrow(messageId).markPolled();
    domainEvents.add(new MessagePolled(id, messageId, now));
  }

  /** Marks a message as delivered to the recipient's UI. Records {@link MessageDelivered}. */
  public void markDelivered(MessageId messageId, Timestamp now) {
    messageOrThrow(messageId).markDelivered();
    domainEvents.add(new MessageDelivered(id, messageId, now));
  }

  private Message messageOrThrow(MessageId messageId) {
    Message message = messages.get(messageId);
    if (message == null) {
      throw new NoSuchMessageException(id, messageId);
    }
    return message;
  }

  private void requireParticipant(UserId userId) {
    if (!isParticipant(userId)) {
      throw new NotAParticipantException(id, userId);
    }
  }

  /**
   * Returns and clears the events recorded since the last call. The
   * application layer drains these after a successful save and publishes
   * them to {@code chat.room.events} (PLAN_SSDLC.md §7.3).
   */
  public List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }
}
