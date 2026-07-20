package com.enterprise.kafka.consumer;

import com.enterprise.common.constants.AppConstants;
import com.enterprise.kafka.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Audit consumer that records every domain event in the audit log.
 * Consumes from a dedicated audit group so it does not interfere with other consumers.
 */
@Slf4j
@Component
public class AuditEventConsumer {

    @KafkaListener(
            topics = {
                    AppConstants.TOPIC_ORDER_CREATED,
                    AppConstants.TOPIC_ORDER_UPDATED,
                    AppConstants.TOPIC_ORDER_CANCELLED,
                    AppConstants.TOPIC_PRODUCT_UPDATED,
                    AppConstants.TOPIC_USER_REGISTERED
            },
            groupId = AppConstants.GROUP_AUDIT,
            containerFactory = "kafkaListenerContainerFactory")
    public void auditAllEvents(
            @Payload DomainEvent event,
            Acknowledgment ack) {

        log.info("[AUDIT] eventType={} eventId={} correlationId={} source={} occurredAt={}",
                event.getEventType(),
                event.getEventId(),
                event.getCorrelationId(),
                event.getSourceService(),
                event.getOccurredAt());

        // In production: persist to audit_log table via AuditLogRepository
        ack.acknowledge();
    }
}
