package com.enterprise.kafka.producer;

import com.enterprise.common.constants.AppConstants;
import com.enterprise.kafka.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Generic Kafka producer that publishes any DomainEvent to the appropriate topic.
 *
 * Design Pattern: Strategy — callers choose the topic; producer handles the transport.
 * Clean Code: single-purpose class, meaningful method name, structured logging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    /**
     * Publishes a domain event to the given Kafka topic.
     *
     * @param topic  Kafka topic name (use {@link AppConstants} constants)
     * @param key    message key — typically the aggregate ID
     * @param event  domain event payload
     * @return future that completes when the broker acknowledges the message
     */
    public CompletableFuture<SendResult<String, DomainEvent>> publish(
            String topic, String key, DomainEvent event) {

        log.info("Publishing event [{}] to topic [{}] with key [{}]",
                event.getEventType(), topic, key);

        CompletableFuture<SendResult<String, DomainEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event [{}] to topic [{}]: {}",
                        event.getEventType(), topic, ex.getMessage(), ex);
            } else {
                log.debug("Event [{}] published to partition [{}] @ offset [{}]",
                        event.getEventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }
}
