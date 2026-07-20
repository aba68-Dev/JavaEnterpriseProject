package com.enterprise.kafka.consumer;

import com.enterprise.common.constants.AppConstants;
import com.enterprise.kafka.event.OrderCancelledEvent;
import com.enterprise.kafka.event.OrderCreatedEvent;
import com.enterprise.kafka.event.OrderUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for all Order-domain events.
 *
 * Clean Code: separate methods per event type, descriptive names.
 * Design Pattern: Event-Driven (Observer) — reacts to changes without coupling services.
 */
@Slf4j
@Component
public class OrderEventConsumer {

    @KafkaListener(
            topics   = AppConstants.TOPIC_ORDER_CREATED,
            groupId  = AppConstants.GROUP_NOTIFICATION,
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCreated(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("ORDER_CREATED event received: orderNumber={}, userId={}, total={} | partition={} offset={}",
                event.getOrderNumber(), event.getUserId(), event.getTotalAmount(), partition, offset);

        try {
            // Downstream logic: send confirmation email, update inventory, etc.
            processOrderCreatedNotification(event);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Error processing ORDER_CREATED event [{}]: {}",
                    event.getOrderNumber(), ex.getMessage(), ex);
            // Do not acknowledge — let Kafka retry based on back-off config
        }
    }

    @KafkaListener(
            topics   = AppConstants.TOPIC_ORDER_UPDATED,
            groupId  = AppConstants.GROUP_NOTIFICATION,
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderUpdated(
            @Payload OrderUpdatedEvent event,
            Acknowledgment ack) {

        log.info("ORDER_UPDATED event received: orderNumber={}, status: {} → {}",
                event.getOrderNumber(), event.getPreviousStatus(), event.getNewStatus());

        processOrderUpdatedNotification(event);
        ack.acknowledge();
    }

    @KafkaListener(
            topics   = AppConstants.TOPIC_ORDER_CANCELLED,
            groupId  = AppConstants.GROUP_NOTIFICATION,
            containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderCancelled(
            @Payload OrderCancelledEvent event,
            Acknowledgment ack) {

        log.info("ORDER_CANCELLED event received: orderNumber={}, reason={}",
                event.getOrderNumber(), event.getCancellationReason());

        processOrderCancelledNotification(event);
        ack.acknowledge();
    }

    // ---- private processing methods ----

    private void processOrderCreatedNotification(OrderCreatedEvent event) {
        log.info("[Notification] Sending order confirmation email to user [{}] for order [{}]",
                event.getUserEmail(), event.getOrderNumber());
    }

    private void processOrderUpdatedNotification(OrderUpdatedEvent event) {
        log.info("[Notification] Sending order status update for order [{}]",
                event.getOrderNumber());
    }

    private void processOrderCancelledNotification(OrderCancelledEvent event) {
        log.info("[Notification] Sending order cancellation notification for order [{}]",
                event.getOrderNumber());
    }
}
