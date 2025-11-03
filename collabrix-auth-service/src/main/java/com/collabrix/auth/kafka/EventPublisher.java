package com.collabrix.auth.kafka;

import com.collabrix.auth.kafka.events.UserDeletedEvent;
import com.collabrix.auth.kafka.events.UserRegisteredEvent;
import com.collabrix.auth.kafka.events.UserRoleChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.user-registered}")
    private String userRegisteredTopic;

    @Value("${kafka.topic.user-deleted}")
    private String userDeletedTopic;

    @Value("${kafka.topic.user-role-changed}")
    private String userRoleChangedTopic;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish UserRegisteredEvent to Kafka
     */
    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            logger.info("Publishing UserRegisteredEvent for user: {}", event.getUsername());

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(userRegisteredTopic, event.getKeycloakUserId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published UserRegisteredEvent for user: {} to topic: {} with offset: {}",
                            event.getUsername(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish UserRegisteredEvent for user: {}", event.getUsername(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing UserRegisteredEvent for user: {}", event.getUsername(), e);
            throw new RuntimeException("Failed to publish user registered event", e);
        }
    }

    /**
     * Publish UserDeletedEvent to Kafka
     */
    public void publishUserDeletedEvent(UserDeletedEvent event) {
        try {
            logger.info("Publishing UserDeletedEvent for user ID: {}", event.getKeycloakUserId());

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(userDeletedTopic, event.getKeycloakUserId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published UserDeletedEvent for user ID: {} to topic: {} with offset: {}",
                            event.getKeycloakUserId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish UserDeletedEvent for user ID: {}", event.getKeycloakUserId(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing UserDeletedEvent for user ID: {}", event.getKeycloakUserId(), e);
            throw new RuntimeException("Failed to publish user deleted event", e);
        }
    }

    /**
     * Publish UserRoleChangedEvent to Kafka
     */
    public void publishUserRoleChangedEvent(UserRoleChangedEvent event) {
        try {
            logger.info("Publishing UserRoleChangedEvent for user ID: {}, role: {}, action: {}",
                    event.getKeycloakUserId(), event.getRoleName(), event.getAction());

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(userRoleChangedTopic, event.getKeycloakUserId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published UserRoleChangedEvent for user ID: {} to topic: {} with offset: {}",
                            event.getKeycloakUserId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish UserRoleChangedEvent for user ID: {}", event.getKeycloakUserId(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing UserRoleChangedEvent for user ID: {}", event.getKeycloakUserId(), e);
            throw new RuntimeException("Failed to publish user role changed event", e);
        }
    }

    /**
     * Generic method to publish any event (for future extensibility)
     */
    public void publishEvent(String topic, String key, Object event) {
        try {
            logger.info("Publishing event to topic: {} with key: {}", topic, key);

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully published event to topic: {} with offset: {}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish event to topic: {}", topic, ex);
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing event to topic: {}", topic, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}