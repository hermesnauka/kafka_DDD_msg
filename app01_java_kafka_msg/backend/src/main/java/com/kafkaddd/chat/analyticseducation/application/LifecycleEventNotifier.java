package com.kafkaddd.chat.analyticseducation.application;

import com.kafkaddd.chat.analyticseducation.domain.LifecycleEvent;

/**
 * Port for pushing a {@link LifecycleEvent} to the live education dashboard
 * over {@code /topic/education/{roomId}} (PLAN_SSDLC.md §7.2, US-2.1/US-2.2).
 * The real STOMP-backed adapter lives in {@code analyticseducation.infrastructure}.
 */
public interface LifecycleEventNotifier {

  void notify(LifecycleEvent event);
}
