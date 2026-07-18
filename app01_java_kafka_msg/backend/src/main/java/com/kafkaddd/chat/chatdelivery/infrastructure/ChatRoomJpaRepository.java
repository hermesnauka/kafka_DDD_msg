package com.kafkaddd.chat.chatdelivery.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ChatRoomJpaRepository extends JpaRepository<ChatRoomEntity, UUID> {}
