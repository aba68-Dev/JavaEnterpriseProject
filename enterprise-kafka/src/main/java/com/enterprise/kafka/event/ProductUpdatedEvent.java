package com.enterprise.kafka.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Raised when a Product's price or stock is updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductUpdatedEvent extends DomainEvent {

    private Long       productId;
    private String     sku;
    private String     name;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private Integer    oldStock;
    private Integer    newStock;
}
