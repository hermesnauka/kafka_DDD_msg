package com.kafkaddd.chat.chatdelivery.infrastructure;

import com.kafkaddd.chat.chatdelivery.application.RoomSummary;
import java.util.List;

record RoomResponse(String id, List<String> participantIds, int messageCount) {

  static RoomResponse from(RoomSummary summary) {
    return new RoomResponse(summary.id(), summary.participantIds(), summary.messageCount());
  }
}
