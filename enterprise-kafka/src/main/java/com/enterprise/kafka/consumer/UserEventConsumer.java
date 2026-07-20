package com.enterprise.kafka.consumer;

import com.enterprise.common.constants.AppConstants;
import com.enterprise.kafka.event.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for User domain events.
 */
@Slf4j
@Component
public class UserEventConsumer {

    @KafkaListener(
            topics   = AppConstants.TOPIC_USER_REGISTERED,
            groupId  = AppConstants.GROUP_NOTIFICATION,
            containerFactory = "kafkaListenerContainerFactory")
    public void handleUserRegistered(
            @Payload UserRegisteredEvent event,
            Acknowledgment ack) {

        log.info("USER_REGISTERED event received: userId={}, email={}",
                event.getUserId(), event.getEmail());

        sendWelcomeEmail(event);
        ack.acknowledge();
    }

    private void sendWelcomeEmail(UserRegisteredEvent event) {
        log.info("[Notification] Sending welcome email to [{}] ({})",
                event.getEmail(), event.getFullName());
    }
}
