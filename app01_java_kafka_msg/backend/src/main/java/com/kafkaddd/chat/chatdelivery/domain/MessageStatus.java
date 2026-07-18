package com.kafkaddd.chat.chatdelivery.domain;

/**
 * A message's lifecycle state, driven by the domain events in
 * PLAN_SSDLC.md §7.4: {@code MessageSent -> PENDING},
 * {@code MessagePolled -> POLLED}, {@code MessageDelivered -> DELIVERED}.
 */
public enum MessageStatus {
  PENDING,
  POLLED,
  DELIVERED
}
