package com.enterprise.rest.service;

import com.enterprise.common.exception.BusinessValidationException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.domain.dto.ProductDto;
import com.enterprise.domain.entity.Category;
import com.enterprise.domain.entity.Product;
import com.enterprise.domain.entity.ProductStatus;
import com.enterprise.domain.mapper.ProductMapper;
import com.enterprise.kafka.producer.KafkaEventProducer;
import com.enterprise.repository.CategoryRepository;
import com.enterprise.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 *
 * Clean Code: given/when/then structure, descriptive test names, one assertion per test.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock ProductRepository  productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductMapper      productMapper;
    @Mock KafkaEventProducer kafkaEventProducer;

    @InjectMocks ProductService productService;

    private Product   sampleProduct;
    private ProductDto sampleDto;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).name("Electronics").build();

        sampleProduct = Product.builder()
                .id(1L)
                .sku("ELEC-001")
                .name("Wireless Headphones")
                .price(new BigDecimal("199.99"))
                .stockQuantity(50)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();

        sampleDto = ProductDto.builder()
                .id(1L)
                .sku("ELEC-001")
                .name("Wireless Headphones")
                .price(new BigDecimal("199.99"))
                .stockQuantity(50)
                .status("ACTIVE")
                .categoryId(1L)
                .build();
    }

    @Test
    @DisplayName("findById returns DTO when product exists")
    void findById_returnsDto_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleDto);

        ProductDto result = productService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("ELEC-001");
    }

    @Test
    @DisplayName("findById throws ResourceNotFoundException when product absent")
    void findById_throwsException_whenProductAbsent() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create throws BusinessValidationException when SKU already exists")
    void create_throwsException_whenSkuDuplicated() {
        when(productRepository.existsBySku("ELEC-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(sampleDto))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("ELEC-001");
    }

    @Test
    @DisplayName("create saves and returns new product DTO")
    void create_savesProduct_whenSkuIsNew() {
        when(productRepository.existsBySku("ELEC-001")).thenReturn(false);
        when(productMapper.toEntity(sampleDto)).thenReturn(sampleProduct);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleProduct.getCategory()));
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleDto);

        ProductDto result = productService.create(sampleDto);

        assertThat(result.getSku()).isEqualTo("ELEC-001");
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("restock throws BusinessValidationException when quantity is zero")
    void restock_throwsException_whenQuantityIsZero() {
        assertThatThrownBy(() -> productService.restock(1L, 0))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("restock publishes Kafka event after updating stock")
    void restock_publishesKafkaEvent_onSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any())).thenReturn(sampleProduct);
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleDto);

        productService.restock(1L, 10);

        verify(kafkaEventProducer).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("findAll returns paged response")
    void findAll_returnsPaged_correctly() {
        when(productRepository.searchProducts(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleProduct)));
        when(productMapper.toDto(sampleProduct)).thenReturn(sampleDto);

        var result = productService.findAll(0, 20, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }
}
