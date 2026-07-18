package com.kafkaddd.chat.analyticseducation.infrastructure;

import com.kafkaddd.chat.analyticseducation.application.LifecycleEventNotifier;
import com.kafkaddd.chat.analyticseducation.domain.LifecycleEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Pushes a {@link LifecycleEvent} to every client subscribed to {@code /topic/education/{roomId}}. */
@Component
class LifecycleEventStompNotifier implements LifecycleEventNotifier {

  private final SimpMessagingTemplate messagingTemplate;

  LifecycleEventStompNotifier(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @Override
  public void notify(LifecycleEvent event) {
    messagingTemplate.convertAndSend("/topic/education/" + event.roomId(), event);
  }
}
