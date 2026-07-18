package com.kafkaddd.chat.chatdelivery.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ParticipantJpaRepository extends JpaRepository<ParticipantEntity, UUID> {

  List<ParticipantEntity> findByRoomId(UUID roomId);

  List<ParticipantEntity> findByUserId(UUID userId);
}
