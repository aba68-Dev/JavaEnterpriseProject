package com.enterprise.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for a single line item within an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private Long id;

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product to order")
    private Long productId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Number of units")
    private Integer quantity;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal unitPrice;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal subtotal;
}
