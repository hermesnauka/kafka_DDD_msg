/**
 * Chat Delivery bounded context — domain layer.
 *
 * <p>Aggregate root: {@link com.kafkaddd.chat.chatdelivery.domain.ChatRoom}.
 * Entities: {@link com.kafkaddd.chat.chatdelivery.domain.Message},
 * {@link com.kafkaddd.chat.chatdelivery.domain.Participant}. Value objects:
 * {@link com.kafkaddd.chat.chatdelivery.domain.MessageContent},
 * {@link com.kafkaddd.chat.chatdelivery.domain.Timestamp},
 * {@link com.kafkaddd.chat.chatdelivery.domain.MessageId},
 * {@link com.kafkaddd.chat.chatdelivery.domain.RoomId},
 * {@link com.kafkaddd.chat.chatdelivery.domain.UserId}. Domain events:
 * {@link com.kafkaddd.chat.chatdelivery.domain.MessageSent},
 * {@link com.kafkaddd.chat.chatdelivery.domain.MessagePolled},
 * {@link com.kafkaddd.chat.chatdelivery.domain.MessageDelivered}
 * (PLAN_SSDLC.md §5.2/§7.4).
 *
 * <p>No Spring, Kafka, or JPA imports belong in this package (NFR-Arch-1;
 * see the repo's AGENTS.md "domain layer has zero framework dependencies"
 * invariant).
 */
package com.kafkaddd.chat.chatdelivery.domain;
