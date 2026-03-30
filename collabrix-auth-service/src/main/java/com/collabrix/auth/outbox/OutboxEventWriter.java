package com.collabrix.auth.outbox;

import com.collabrix.auth.dto.UserRegisterRequest;
import com.collabrix.auth.dto.UserResponse;
import com.collabrix.auth.entity.AuthOutboxEvent;
import com.collabrix.auth.repository.AuthOutboxEventRepository;
import com.collabrix.common.libraries.dto.EventTypeEnum;
import com.collabrix.common.libraries.dto.KafkaEventStatus;
import com.collabrix.common.libraries.events.UserLoginEvent;
import com.collabrix.common.libraries.events.UserLogoutEvent;
import com.collabrix.common.libraries.events.UserRegisteredEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventWriter {

    private final AuthOutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    /**
     * Write USER_REGISTERED event to outbox
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW) // New transaction
    public void writeUserRegisteredEvent(UserResponse authUser, UserRegisterRequest req) {
        log.info("📝 Creating USER_REGISTERED event for user: {}", authUser.getUsername());

        UserRegisteredEvent payload = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventTypeEnum.USER_REGISTERED)
                .timestamp(System.currentTimeMillis())
                .keycloakUserId(authUser.getId())
                .username(authUser.getUsername())
                .email(authUser.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .build();

        write(payload.getEventType(), payload.getEventId(), payload);
    }

    /**
     * Write USER_LOGIN event to outbox
     */
    @Transactional
    public void writeUserLoginEvent(String userId, String email, String ip, String agent) {
        log.info("📝 Creating USER_LOGIN event for user: {}", userId);

        UserLoginEvent payload = UserLoginEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventTypeEnum.USER_LOGIN)
                .timestamp(System.currentTimeMillis())
                .keycloakUserId(userId)
                .email(email)
                .ipAddress(ip)
                .userAgent(agent)
                .build();

        write(payload.getEventType(), payload.getEventId(), payload);
    }

    /**
     * Write USER_LOGOUT event to outbox
     */
    @Transactional
    public void writeUserLogoutEvent(String userId) {
        log.info("📝 Creating USER_LOGOUT event for user: {}", userId);

        UserLogoutEvent payload = UserLogoutEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventTypeEnum.USER_LOGOUT)
                .timestamp(System.currentTimeMillis())
                .keycloakUserId(userId)
                .build();

        write(EventTypeEnum.USER_LOGOUT, payload.getEventId(), payload);
    }

    /**
     * Generic outbox writer
     */
    private <T> void write(EventTypeEnum type, UUID eventId, T payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            log.debug("📄 Payload JSON: {}", payloadJson);

            AuthOutboxEvent event = AuthOutboxEvent.builder()
                    .eventId(eventId)
                    .eventType(type)
                    .payload(payloadJson)
                    .status(KafkaEventStatus.PENDING)
                    .retryCount(0)
                    .build();

            AuthOutboxEvent saved = outboxRepo.save(event);

            log.info("✅ Outbox event stored: [{}] ID={}", type, saved.getEventId());

        } catch (Exception e) {
            log.error("❌ Failed to write outbox event: {}", e.getMessage(), e);
            throw new RuntimeException("Outbox write failed for " + type, e);
        }
    }
}