package com.enterprise.rest.service;

import com.enterprise.common.exception.BusinessValidationException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.domain.dto.OrderDto;
import com.enterprise.domain.dto.OrderItemDto;
import com.enterprise.domain.entity.*;
import com.enterprise.domain.mapper.OrderMapper;
import com.enterprise.kafka.producer.KafkaEventProducer;
import com.enterprise.repository.OrderRepository;
import com.enterprise.repository.ProductRepository;
import com.enterprise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock OrderRepository   orderRepository;
    @Mock UserRepository    userRepository;
    @Mock ProductRepository productRepository;
    @Mock OrderMapper       orderMapper;
    @Mock KafkaEventProducer kafkaEventProducer;

    @InjectMocks OrderService orderService;

    private User    testUser;
    private Product testProduct;
    private Order   testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .build();

        testProduct = Product.builder()
                .id(10L)
                .sku("ELEC-001")
                .name("Headphones")
                .price(new BigDecimal("99.99"))
                .stockQuantity(20)
                .status(ProductStatus.ACTIVE)
                .build();

        testOrder = Order.builder()
                .id(100L)
                .orderNumber("ORD-ABCD1234")
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.99"))
                .build();
    }

    @Test
    @DisplayName("createOrder throws exception when user not found")
    void createOrder_throwsException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        OrderDto dto = OrderDto.builder()
                .items(List.of(OrderItemDto.builder().productId(10L).quantity(1).build()))
                .build();

        assertThatThrownBy(() -> orderService.createOrder(999L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createOrder throws exception when product has insufficient stock")
    void createOrder_throwsException_whenInsufficientStock() {
        testProduct.setStockQuantity(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        OrderDto dto = OrderDto.builder()
                .items(List.of(OrderItemDto.builder().productId(10L).quantity(5).build()))
                .build();

        assertThatThrownBy(() -> orderService.createOrder(1L, dto))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("unavailable or has insufficient stock");
    }

    @Test
    @DisplayName("cancelOrder throws exception when order is already shipped")
    void cancelOrder_throwsException_whenOrderNotCancellable() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(100L, "Changed mind"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    @DisplayName("cancelOrder succeeds and publishes Kafka event")
    void cancelOrder_succeedsAndPublishesEvent_whenCancellable() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        orderService.cancelOrder(100L, "Duplicate order");

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(kafkaEventProducer).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("findById throws exception when order absent")
    void findById_throwsException_whenOrderAbsent() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
