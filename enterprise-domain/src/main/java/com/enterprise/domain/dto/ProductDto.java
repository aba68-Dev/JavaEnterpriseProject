package com.enterprise.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Product resource — decouples API contract from JPA entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product resource")
public class ProductDto {

    @Schema(description = "Internal product ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    @Schema(description = "Stock-keeping unit (unique identifier)")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    @Schema(description = "Display name of the product")
    private String name;

    @Schema(description = "Detailed product description")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Unit price")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    @Schema(description = "Available stock quantity")
    private Integer stockQuantity;

    @Schema(description = "Product status")
    private String status;

    @Schema(description = "Category ID")
    private Long categoryId;

    @Schema(description = "Category name", accessMode = Schema.AccessMode.READ_ONLY)
    private String categoryName;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
