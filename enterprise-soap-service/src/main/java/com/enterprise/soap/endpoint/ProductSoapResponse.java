package com.enterprise.soap.endpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SOAP response DTO for Product information.
 * Annotated with JAXB for XML marshalling/unmarshalling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Product", namespace = "http://enterprise.com/soap/product")
public class ProductSoapResponse {

    @XmlElement(required = true)
    private Long id;

    @XmlElement(required = true)
    private String sku;

    @XmlElement(required = true)
    private String name;

    private String description;

    @XmlElement(required = true)
    private BigDecimal price;

    @XmlElement(required = true)
    private Integer stockQuantity;

    private String status;
    private String categoryName;
    private LocalDateTime createdAt;
}
