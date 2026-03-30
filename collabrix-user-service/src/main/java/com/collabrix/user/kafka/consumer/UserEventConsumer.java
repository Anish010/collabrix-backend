package com.collabrix.user.kafka.consumer;

import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.kafka.events.UserDeletedEvent;
import com.collabrix.user.kafka.events.UserRegisteredEvent;
import com.collabrix.user.kafka.events.UserRoleChangedEvent;
import com.collabrix.user.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for user-related events from auth-service
 *
 * Listens to topics:
 * - user.registered: Creates new user profiles
 * - user.deleted: Soft deletes user profiles
 * - user.role.changed: Logs role changes (optional processing)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    /**
     * Consume USER_REGISTERED events
     * Creates a new user profile when a user registers in auth-service
     */
    @KafkaListener(
            topics = "${kafka.topic.user-registered}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void consumeUserRegisteredEvent(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📨 Received USER_REGISTERED event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

        log.info("🔍 Processing USER_REGISTERED event: eventId={}, username={}, email={}",
                event.getEventId(), event.getUsername(), event.getEmail());

        try {
            // Create user profile
            UserProfileResponse profile = userProfileService.createProfile(event);
            log.info("✅ Successfully created profile for user: {} (ID: {})",
                    profile.getUsername(), profile.getId());
        } catch (Exception ex) {
            log.error("❌ Failed to process USER_REGISTERED event: {}", event, ex);
        }
    }


    /**
     * Consume USER_DELETED events
     * Soft deletes the user profile when user is deleted from auth-service
     */
    @KafkaListener(
            topics = "${kafka.topic.user-deleted}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userDeletedKafkaListenerContainerFactory"
    )
    public void consumeUserDeletedEvent(
            @Payload UserDeletedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📨 Received USER_DELETED event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

        try {
            log.info("🔍 Processing USER_DELETED event: eventId={}, username={}, keycloakUserId={}",
                    event.getEventId(), event.getUsername(), event.getKeycloakUserId());

            // Soft delete user profile
            userProfileService.deleteProfile(event.getKeycloakUserId());

            log.info("✅ Successfully deleted profile for user: {} (ID: {})",
                    event.getUsername(), event.getKeycloakUserId());

        } catch (Exception ex) {
            log.error("❌ Failed to process USER_DELETED event: {}",  event, ex);
        }
    }

    /**
     * Consume USER_ROLE_CHANGED events
     * Currently just logs the event, but can be extended for role-based profile updates
     */
    @KafkaListener(
            topics = "${kafka.topic.user-role-changed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userRoleChangedKafkaListenerContainerFactory"
    )
    public void consumeUserRoleChangedEvent(
            @Payload UserRoleChangedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📨 Received USER_ROLE_CHANGED event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

        try {
            log.info("🔍 Processing USER_ROLE_CHANGED event: eventId={}, username={}, role={}, action={}",
                    event.getEventId(), event.getUsername(), event.getRoleName(), event.getAction());

            // Fetch user profile
            UserProfileResponse response;
            if ("ASSIGNED".equalsIgnoreCase(event.getAction())) {
                response = userProfileService.addRole(event.getKeycloakUserId(), event.getRoleName());
                log.info("✅ Role '{}' assigned to user {}", event.getRoleName(), event.getKeycloakUserId());
            } else if ("REMOVED".equalsIgnoreCase(event.getAction())) {
                response = userProfileService.removeRole(event.getKeycloakUserId(), event.getRoleName());
                log.info("✅ Role '{}' removed from user {}", event.getRoleName(), event.getKeycloakUserId());
            } else {
                log.warn("⚠️ Unknown role action '{}' for user {}", event.getAction(), event.getKeycloakUserId());
            }

        } catch (Exception ex) {
            log.error("❌ Failed to process USER_ROLE_CHANGED event: {}", event, ex);
        }
    }
}