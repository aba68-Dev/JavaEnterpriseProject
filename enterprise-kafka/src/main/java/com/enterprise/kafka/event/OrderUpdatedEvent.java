package com.enterprise.kafka.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Raised when an Order's status changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderUpdatedEvent extends DomainEvent {

    private Long   orderId;
    private String orderNumber;
    private String previousStatus;
    private String newStatus;
    private BigDecimal totalAmount;
}
