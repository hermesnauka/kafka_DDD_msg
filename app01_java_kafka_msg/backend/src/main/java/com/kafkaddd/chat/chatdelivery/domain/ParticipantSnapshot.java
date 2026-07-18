package com.kafkaddd.chat.chatdelivery.domain;

/**
 * Plain data carrier for {@link ChatRoom#reconstitute} — lets the
 * repository adapter (a different package) hand reconstruction data to
 * {@code ChatRoom} without ever constructing a {@link Participant} itself.
 */
public record ParticipantSnapshot(UserId userId, Timestamp joinedAt) {}
