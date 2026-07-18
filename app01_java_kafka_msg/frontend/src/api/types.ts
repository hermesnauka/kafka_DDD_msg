// Mirrors the backend response DTOs (identity.infrastructure.UserResponse,
// chatdelivery.infrastructure.RoomResponse/MessageResponse) and the
// educational.telemetry payload shape
// (analyticseducation.domain.LifecycleEvent). Kept in sync by hand — no
// codegen step exists yet.

export interface User {
  id: string
  email: string
  displayName: string
}

export interface Room {
  id: string
  participantIds: string[]
  messageCount: number
}

export interface ChatMessage {
  id: string
  senderId: string
  content: string
  sentAt: string
  status: 'PENDING' | 'POLLED' | 'DELIVERED'
}

// analyticseducation.domain.LifecycleEvent, pushed to
// /topic/education/{roomId} — metadata only, never message content (FR-6).
export interface LifecycleEvent {
  eventType: 'MessageSent' | 'MessagePolled' | 'MessageDelivered'
  roomId: string
  messageId: string
  kafkaPartition: number
  kafkaOffset: number
  consumerGroup: string
  occurredAt: string
}
