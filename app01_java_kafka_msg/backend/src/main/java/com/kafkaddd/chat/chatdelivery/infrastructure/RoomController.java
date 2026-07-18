package com.kafkaddd.chat.chatdelivery.infrastructure;

import com.kafkaddd.chat.chatdelivery.application.CreateRoomUseCase;
import com.kafkaddd.chat.chatdelivery.application.ListMessagesUseCase;
import com.kafkaddd.chat.chatdelivery.application.ListRoomsUseCase;
import com.kafkaddd.chat.chatdelivery.application.MessageView;
import com.kafkaddd.chat.chatdelivery.application.RoomSummary;
import com.kafkaddd.chat.chatdelivery.application.SendMessageUseCase;
import com.kafkaddd.chat.chatdelivery.domain.RoomId;
import com.kafkaddd.chat.chatdelivery.domain.UserId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PLAN_SSDLC.md §7.1. Every method converts the authenticated principal
 * (an {@code identity.domain.UserId}, set by {@code JwtAuthenticationFilter})
 * into this context's own {@code UserId} at the boundary — the two types
 * wrap the same UUID but are never the same class (see
 * chatdelivery.domain's package-info on bounded-context separation).
 */
@RestController
@RequestMapping("/api/v1/rooms")
class RoomController {

  private final CreateRoomUseCase createRoomUseCase;
  private final SendMessageUseCase sendMessageUseCase;
  private final ListRoomsUseCase listRoomsUseCase;
  private final ListMessagesUseCase listMessagesUseCase;

  RoomController(
      CreateRoomUseCase createRoomUseCase,
      SendMessageUseCase sendMessageUseCase,
      ListRoomsUseCase listRoomsUseCase,
      ListMessagesUseCase listMessagesUseCase) {
    this.createRoomUseCase = createRoomUseCase;
    this.sendMessageUseCase = sendMessageUseCase;
    this.listRoomsUseCase = listRoomsUseCase;
    this.listMessagesUseCase = listMessagesUseCase;
  }

  @GetMapping
  List<RoomResponse> list(Authentication authentication) {
    return listRoomsUseCase.listForUser(currentUserId(authentication)).stream().map(RoomResponse::from).toList();
  }

  @PostMapping
  ResponseEntity<RoomResponse> create(@Valid @RequestBody CreateRoomRequest request, Authentication authentication) {
    List<UserId> others = request.participantIds().stream().map(UUID::fromString).map(UserId::new).toList();
    RoomSummary summary = createRoomUseCase.create(currentUserId(authentication), others);
    return ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse.from(summary));
  }

  @GetMapping("/{roomId}/messages")
  List<MessageResponse> messages(@PathVariable String roomId, Authentication authentication) {
    RoomId id = new RoomId(UUID.fromString(roomId));
    List<MessageView> messages = listMessagesUseCase.list(id, currentUserId(authentication));
    return messages.stream().map(MessageResponse::from).toList();
  }

  @PostMapping("/{roomId}/messages")
  ResponseEntity<MessageResponse> send(
      @PathVariable String roomId, @Valid @RequestBody SendMessageRequest request, Authentication authentication) {
    RoomId id = new RoomId(UUID.fromString(roomId));
    MessageView message = sendMessageUseCase.send(id, currentUserId(authentication), request.content());
    return ResponseEntity.status(HttpStatus.CREATED).body(MessageResponse.from(message));
  }

  private UserId currentUserId(Authentication authentication) {
    com.kafkaddd.chat.identity.domain.UserId identityUserId =
        (com.kafkaddd.chat.identity.domain.UserId) authentication.getPrincipal();
    return new UserId(identityUserId.value());
  }
}
