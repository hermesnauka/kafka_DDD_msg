package com.kafkaddd.chat.chatdelivery.application;

import com.kafkaddd.chat.chatdelivery.domain.Message;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;

/**
 * Port for pushing a delivered message to live clients over
 * {@code /topic/rooms/{roomId}} (PLAN_SSDLC.md §7.2). The real STOMP-backed
 * adapter lives in {@code chatdelivery.infrastructure}.
 */
public interface MessageDeliveryNotifier {

  void notifyDelivered(RoomId roomId, Message message);
}
