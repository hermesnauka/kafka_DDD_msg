package com.kafkaddd.chat.chatdelivery.domain;

/**
 * Plain data carrier for {@link ChatRoom#reconstitute} — lets the
 * repository adapter (a different package) hand reconstruction data to
 * {@code ChatRoom} without ever constructing a {@link Message} itself.
 */
public record MessageSnapshot(
    MessageId id, UserId senderId, MessageContent content, Timestamp sentAt, MessageStatus status) {}
