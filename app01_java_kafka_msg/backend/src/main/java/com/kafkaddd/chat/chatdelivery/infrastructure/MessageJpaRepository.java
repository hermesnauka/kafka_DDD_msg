package com.kafkaddd.chat.chatdelivery.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MessageJpaRepository extends JpaRepository<MessageEntity, UUID> {

  List<MessageEntity> findByRoomIdOrderBySentAtAsc(UUID roomId);
}
