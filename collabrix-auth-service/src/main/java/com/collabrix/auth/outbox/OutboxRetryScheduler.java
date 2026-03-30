package com.collabrix.auth.outbox;

import com.collabrix.auth.entity.AuthOutboxEvent;
import com.collabrix.auth.repository.AuthOutboxEventRepository;
import com.collabrix.common.libraries.dto.EventTypeEnum;
import com.collabrix.common.libraries.dto.KafkaEventStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRetryScheduler {

    private final AuthOutboxEventRepository repo;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void retryOutboxEvents() {

        List<AuthOutboxEvent> events =
                repo.findTop50ByStatusInOrderByCreatedAtAsc(
                        List.of(KafkaEventStatus.PENDING, KafkaEventStatus.FAILED)
                );

        if (events.isEmpty()) return;

        for (AuthOutboxEvent ev : events) {
            try {
                Object payload = objectMapper.readValue(ev.getPayload(), Object.class);

                kafkaTemplate.send(
                        resolveTopic(ev.getEventType()),
                        ev.getEventId().toString(),
                        payload
                ).whenComplete((res, ex) -> {
                    if (ex == null) {
                        ev.setStatus(KafkaEventStatus.COMPLETED);
                        ev.setSentAt(LocalDateTime.now());
                    } else {
                        ev.setRetryCount(ev.getRetryCount() + 1);
                        ev.setLastError(ex.getMessage());
                        ev.setStatus(KafkaEventStatus.FAILED);
                    }
                    repo.save(ev);
                });

            } catch (Exception ex) {
                ev.setRetryCount(ev.getRetryCount() + 1);
                ev.setLastError(ex.getMessage());
                ev.setStatus(KafkaEventStatus.FAILED);
                repo.save(ev);
            }
        }
    }

    private String resolveTopic(EventTypeEnum type) {
        return switch (type) {
            case USER_REGISTERED -> "user.registered";
            case USER_LOGIN -> "user.login";
            case USER_LOGOUT -> "user.logout";
            case USER_ROLE_CHANGED -> "user.role.changed";
            case USER_DELETED -> "user.deleted";
        };
    }
}
