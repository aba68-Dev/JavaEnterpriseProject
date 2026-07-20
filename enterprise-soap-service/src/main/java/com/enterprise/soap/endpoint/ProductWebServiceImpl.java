package com.enterprise.soap.endpoint;

import com.enterprise.domain.entity.Product;
import com.enterprise.domain.entity.ProductStatus;
import com.enterprise.repository.ProductRepository;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JAX-WS SOAP endpoint implementation for Product information services.
 *
 * Business use-case: legacy B2B partners or ERP systems query product data via SOAP.
 *
 * Design Pattern: Adapter — wraps the Spring Data repository behind a SOAP contract.
 * Clean Code: thin endpoint, delegates to shared repository, returns XML-safe DTOs.
 */
@Slf4j
@Service
@WebService(
        serviceName     = "ProductService",
        portName        = "ProductPort",
        endpointInterface = "com.enterprise.soap.endpoint.ProductWebService",
        targetNamespace   = "http://enterprise.com/soap/product"
)
@RequiredArgsConstructor
public class ProductWebServiceImpl implements ProductWebService {

    private final ProductRepository productRepository;

    @Override
    public ProductSoapResponse getProductById(Long productId) {
        log.info("SOAP getProductById: productId={}", productId);
        return productRepository.findById(productId)
                .map(this::toSoapResponse)
                .orElseThrow(() -> new RuntimeException("Product not found: id=" + productId));
    }

    @Override
    public ProductSoapResponse getProductBySku(String sku) {
        log.info("SOAP getProductBySku: sku={}", sku);
        return productRepository.findBySku(sku)
                .map(this::toSoapResponse)
                .orElseThrow(() -> new RuntimeException("Product not found: sku=" + sku));
    }

    @Override
    public List<ProductSoapResponse> searchProducts(Long categoryId, BigDecimal minPrice,
                                                     BigDecimal maxPrice, String keyword) {
        log.info("SOAP searchProducts: categoryId={}, keyword={}", categoryId, keyword);
        return productRepository.searchProducts(
                        categoryId, minPrice, maxPrice, keyword,
                        PageRequest.of(0, 50))
                .getContent().stream()
                .map(this::toSoapResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateProductStock(Long productId, int quantity) {
        log.info("SOAP updateProductStock: productId={}, quantity={}", productId, quantity);
        return productRepository.findById(productId).map(product -> {
            if (quantity < 0) {
                product.deductStock(Math.abs(quantity));
            } else {
                product.restock(quantity);
            }
            productRepository.save(product);
            return true;
        }).orElse(false);
    }

    // ─── Private mapper ───────────────────────────────────────────────────────

    private ProductSoapResponse toSoapResponse(Product product) {
        return ProductSoapResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus().name())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
