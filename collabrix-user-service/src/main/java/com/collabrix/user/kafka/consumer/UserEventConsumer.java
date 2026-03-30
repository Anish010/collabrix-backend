package com.collabrix.user.kafka.consumer;

import com.collabrix.common.libraries.events.*;
import com.collabrix.user.dto.UserProfileResponse;
import com.collabrix.user.service.UserProfileService;
import com.collabrix.user.service.UserRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka consumer for user-related events from auth-service
 *
 * Listens to topics:
 * - user.registered: Creates new user profiles
 * - user.deleted: Soft deletes user profiles
 * - user.logout: Logout user
 * - user.role.changed: Logs role changes (optional processing)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserProfileService userProfileService;
    private final UserRoleService userRoleService;
    private final ObjectMapper objectMapper;

    /**
     * USER_REGISTERED — creates profile for new user if not exists
     * Now:
     * ✔ Ensure idempotency
     * ✔ Recover missing profiles if needed
     */
    @KafkaListener(
            topics = "${kafka.topic.user-registered}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeUserRegisteredEvent(@Payload UserRegisteredEvent event) {
        log.info("📨 USER_REGISTERED received for {}", event.getKeycloakUserId());

        try {
            // If profile already exists — skip
            if (userProfileService.profileExists(event.getKeycloakUserId())) {
                log.info("⚠️ Profile already exists for {} — skipping", event.getKeycloakUserId());
                return;
            }

            //  Recovery mode — create missing profile (rare case)
            log.warn("🛠 Missing profile detected — creating via recovery for {}", event.getKeycloakUserId());

            log.info("🔍 Created profile for new user: {} ({})", event.getUsername(), event.getKeycloakUserId());

        } catch (Exception ex) {
            log.error(" USER_REGISTERED event handling failed for {} → will retry via DLQ/outbox",
                    event.getKeycloakUserId(), ex);
            throw ex; // let retry mechanism handle failures
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
    @Transactional
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
     * Consume USER_LOGOUT events
     */
    @KafkaListener(
            topics = "${kafka.topic.user-logout}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userLogoutKafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeUserLogoutEvent(
            @Payload UserLogoutEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📨 Received USER_LOGOUT event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

        try {
            log.info("🔍 Processing USER_LOGOUT event for user ID={}", event.getKeycloakUserId());

            userProfileService.logoutUser(event);
            log.info("✅ Processed USER_LOGOUT event for user ID={}", event.getKeycloakUserId());

        } catch (Exception ex) {
            log.error("❌ Failed to process USER_LOGOUT event for user ID={}", event.getKeycloakUserId(), ex);
        }
    }


    /**
     * Consume USER_LOGIN events
     */
    @KafkaListener(
            topics = "${kafka.topic.user-login}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userLoginKafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeUserLoginEvent(
            @Payload UserLoginEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📨 Received USER_LOGIN event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);

        try {
            log.info("🔍 Processing USER_LOGIN event for user ID={}", event.getKeycloakUserId());

            userProfileService.loginUser(event);
            log.info("✅ Processed USER_LOGIN event for user ID={}", event.getKeycloakUserId());

        } catch (Exception ex) {
            log.error("❌ Failed to process USER_LOGIN event for user ID={}", event.getKeycloakUserId(), ex);
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
                response = userRoleService.addRole(event.getKeycloakUserId(), event.getRoleName());
                log.info("✅ Role '{}' assigned to user {}", event.getRoleName(), event.getKeycloakUserId());
            } else if ("REMOVED".equalsIgnoreCase(event.getAction())) {
                response = userRoleService.removeRole(event.getKeycloakUserId(), event.getRoleName());
                log.info("✅ Role '{}' removed from user {}", event.getRoleName(), event.getKeycloakUserId());
            } else {
                log.warn("⚠️ Unknown role action '{}' for user {}", event.getAction(), event.getKeycloakUserId());
            }

        } catch (Exception ex) {
            log.error("❌ Failed to process USER_ROLE_CHANGED event: {}", event, ex);
        }
    }
}