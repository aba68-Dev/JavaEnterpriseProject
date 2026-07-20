package com.enterprise.kafka.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base class for all Kafka domain events.
 * Design Pattern: Observer / Event-Driven — events decouple producers from consumers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderCreatedEvent.class,    name = "ORDER_CREATED"),
        @JsonSubTypes.Type(value = OrderUpdatedEvent.class,    name = "ORDER_UPDATED"),
        @JsonSubTypes.Type(value = OrderCancelledEvent.class,  name = "ORDER_CANCELLED"),
        @JsonSubTypes.Type(value = ProductUpdatedEvent.class,  name = "PRODUCT_UPDATED"),
        @JsonSubTypes.Type(value = UserRegisteredEvent.class,  name = "USER_REGISTERED"),
})
public abstract class DomainEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime occurredAt;
    private String correlationId;
    private String sourceService;
}
