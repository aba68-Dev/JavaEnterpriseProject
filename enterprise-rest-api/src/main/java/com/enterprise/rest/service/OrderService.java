package com.enterprise.rest.service;

import com.enterprise.common.exception.BusinessValidationException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.common.model.PagedResponse;
import com.enterprise.domain.dto.OrderDto;
import com.enterprise.domain.dto.OrderItemDto;
import com.enterprise.domain.entity.*;
import com.enterprise.domain.mapper.OrderMapper;
import com.enterprise.kafka.event.OrderCancelledEvent;
import com.enterprise.kafka.event.OrderCreatedEvent;
import com.enterprise.kafka.event.OrderUpdatedEvent;
import com.enterprise.kafka.producer.KafkaEventProducer;
import com.enterprise.repository.OrderRepository;
import com.enterprise.repository.ProductRepository;
import com.enterprise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.enterprise.common.constants.AppConstants.*;

/**
 * Business service for Order lifecycle management.
 *
 * Design Pattern: Facade — orchestrates User, Product, Order repositories,
 * Kafka event publishing, and business rule enforcement.
 *
 * Clean Code: guard clauses, single-purpose methods, no nested conditionals.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository   orderRepository;
    private final UserRepository    userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper       orderMapper;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional(readOnly = true)
    public PagedResponse<OrderDto> findAll(int page, int size) {
        Page<Order> orders = orderRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.of(
                orders.getContent().stream().map(orderMapper::toDto).toList(),
                page, size, orders.getTotalElements());
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return orderMapper.toDto(getOrderOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderDto> findByUser(Long userId, int page, int size) {
        Page<Order> orders = orderRepository.findByUserId(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.of(
                orders.getContent().stream().map(orderMapper::toDto).toList(),
                page, size, orders.getTotalElements());
    }

    public OrderDto createOrder(Long userId, OrderDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .shippingAddress(dto.getShippingAddress())
                .notes(dto.getNotes())
                .build();

        for (OrderItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDto.getProductId()));

            if (!product.isAvailableForPurchase(itemDto.getQuantity())) {
                throw new BusinessValidationException(
                        "Product [" + product.getSku() + "] is unavailable or has insufficient stock");
            }

            product.deductStock(itemDto.getQuantity());
            productRepository.save(product);

            order.addItem(OrderItem.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(product.getPrice())
                    .build());
        }

        Order saved = orderRepository.save(order);
        log.info("Order created: id={}, number={}, user={}", saved.getId(), saved.getOrderNumber(), userId);

        publishOrderCreatedEvent(saved, user.getEmail());
        return orderMapper.toDto(saved);
    }

    public OrderDto updateStatus(Long id, OrderStatus newStatus) {
        Order order = getOrderOrThrow(id);
        OrderStatus previousStatus = order.getStatus();

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        publishOrderUpdatedEvent(updated, previousStatus);
        return orderMapper.toDto(updated);
    }

    public void cancelOrder(Long id, String reason) {
        Order order = getOrderOrThrow(id);

        if (!order.isCancellable()) {
            throw new BusinessValidationException(
                    "Order [" + order.getOrderNumber() + "] cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        publishOrderCancelledEvent(order, reason);
        log.info("Order cancelled: id={}, reason={}", id, reason);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void publishOrderCreatedEvent(Order order, String userEmail) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .userEmail(userEmail)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
        enrichEvent(event, "ORDER_CREATED");
        kafkaEventProducer.publish(TOPIC_ORDER_CREATED, order.getOrderNumber(), event);
    }

    private void publishOrderUpdatedEvent(Order order, OrderStatus previousStatus) {
        OrderUpdatedEvent event = OrderUpdatedEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .previousStatus(previousStatus.name())
                .newStatus(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .build();
        enrichEvent(event, "ORDER_UPDATED");
        kafkaEventProducer.publish(TOPIC_ORDER_UPDATED, order.getOrderNumber(), event);
    }

    private void publishOrderCancelledEvent(Order order, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .cancellationReason(reason)
                .build();
        enrichEvent(event, "ORDER_CANCELLED");
        kafkaEventProducer.publish(TOPIC_ORDER_CANCELLED, order.getOrderNumber(), event);
    }

    private void enrichEvent(com.enterprise.kafka.event.DomainEvent event, String type) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(type);
        event.setOccurredAt(LocalDateTime.now());
        event.setSourceService("enterprise-rest-api");
    }
}
