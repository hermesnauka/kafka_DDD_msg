/**
 * Analytics &amp; Education bounded context — infrastructure layer.
 * Read-only Kafka consumers for chat.room.events metadata and
 * educational.telemetry (never message content, FR-6); STOMP publisher for
 * the education dashboard.
 */
package com.kafkaddd.chat.analyticseducation.infrastructure;
