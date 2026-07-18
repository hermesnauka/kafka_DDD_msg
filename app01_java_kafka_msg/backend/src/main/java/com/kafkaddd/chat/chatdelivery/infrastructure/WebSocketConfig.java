package com.kafkaddd.chat.chatdelivery.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket (PLAN_SSDLC.md §7.2): {@code /topic/**} for
 * server-to-client broadcast, {@code /app/**} for client-to-server. The
 * {@code /ws} handshake endpoint goes through the same Spring Security
 * filter chain as everything else — {@link com.kafkaddd.chat.identity.infrastructure.SecurityConfig}'s
 * {@code anyRequest().authenticated()} already covers it, so only a caller
 * with a valid {@code access_token} cookie can open a STOMP connection.
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws");
  }
}
