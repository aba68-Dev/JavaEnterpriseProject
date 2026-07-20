package com.enterprise.kafka.event;

import lombok.*;

/**
 * Raised when an Order is cancelled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCancelledEvent extends DomainEvent {

    private Long   orderId;
    private String orderNumber;
    private Long   userId;
    private String cancellationReason;
}
