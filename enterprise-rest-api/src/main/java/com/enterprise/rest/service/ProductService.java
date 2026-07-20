package com.enterprise.rest.service;

import com.enterprise.common.exception.BusinessValidationException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.common.model.PagedResponse;
import com.enterprise.domain.dto.ProductDto;
import com.enterprise.domain.entity.Category;
import com.enterprise.domain.entity.Product;
import com.enterprise.domain.entity.ProductStatus;
import com.enterprise.domain.mapper.ProductMapper;
import com.enterprise.kafka.event.ProductUpdatedEvent;
import com.enterprise.kafka.producer.KafkaEventProducer;
import com.enterprise.repository.CategoryRepository;
import com.enterprise.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.enterprise.common.constants.AppConstants.*;

/**
 * Business service for Product operations.
 *
 * Clean Code:
 *  - Single Responsibility: only product business logic
 *  - Meaningful names, no magic strings
 *  - Guard clauses instead of nested ifs
 *
 * Design Patterns:
 *  - Facade: hides repository + kafka + mapper complexity behind one interface
 *  - Template Method: uniform response construction
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper      productMapper;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> findAll(int page, int size, Long categoryId,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             String search) {
        Page<Product> products = productRepository.searchProducts(
                categoryId, minPrice, maxPrice, search,
                PageRequest.of(page, size, Sort.by("name")));

        return PagedResponse.of(
                products.getContent().stream().map(productMapper::toDto).toList(),
                page, size, products.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return productMapper.toDto(getProductOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProductDto findBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku=" + sku));
    }

    public ProductDto create(ProductDto dto) {
        if (productRepository.existsBySku(dto.getSku())) {
            throw new BusinessValidationException("Product with SKU [" + dto.getSku() + "] already exists");
        }

        Product product = productMapper.toEntity(dto);
        resolveAndSetCategory(product, dto.getCategoryId());

        Product saved = productRepository.save(product);
        log.info("Product created: id={}, sku={}", saved.getId(), saved.getSku());
        return productMapper.toDto(saved);
    }

    public ProductDto update(Long id, ProductDto dto) {
        Product existing = getProductOrThrow(id);
        BigDecimal oldPrice = existing.getPrice();
        int oldStock = existing.getStockQuantity();

        productMapper.updateEntityFromDto(dto, existing);
        resolveAndSetCategory(existing, dto.getCategoryId());

        Product updated = productRepository.save(existing);
        publishProductUpdatedEvent(updated, oldPrice, oldStock);
        return productMapper.toDto(updated);
    }

    public void delete(Long id) {
        Product product = getProductOrThrow(id);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
        log.info("Product soft-deleted: id={}", id);
    }

    public ProductDto restock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new BusinessValidationException("Restock quantity must be positive");
        }
        Product product = getProductOrThrow(id);
        int oldStock = product.getStockQuantity();
        product.restock(quantity);
        Product updated = productRepository.save(product);
        publishProductUpdatedEvent(updated, updated.getPrice(), oldStock);
        return productMapper.toDto(updated);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private void resolveAndSetCategory(Product product, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
            product.setCategory(category);
        }
    }

    private void publishProductUpdatedEvent(Product product, BigDecimal oldPrice, int oldStock) {
        ProductUpdatedEvent event = ProductUpdatedEvent.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .oldPrice(oldPrice)
                .newPrice(product.getPrice())
                .oldStock(oldStock)
                .newStock(product.getStockQuantity())
                .build();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("PRODUCT_UPDATED");
        event.setOccurredAt(LocalDateTime.now());
        event.setSourceService("enterprise-rest-api");

        kafkaEventProducer.publish(TOPIC_PRODUCT_UPDATED, String.valueOf(product.getId()), event);
    }
}
