package com.collabrix.auth.repository;

import com.collabrix.auth.entity.AuthOutboxEvent;
import com.collabrix.common.libraries.dto.KafkaEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuthOutboxEventRepository extends JpaRepository<AuthOutboxEvent, UUID> {

    List<AuthOutboxEvent> findByStatus(KafkaEventStatus status);

    long countByStatus(KafkaEventStatus status);

    List<AuthOutboxEvent> findTop50ByStatusInOrderByCreatedAtAsc(List<KafkaEventStatus> statuses);


}