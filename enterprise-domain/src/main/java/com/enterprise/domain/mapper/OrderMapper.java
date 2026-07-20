package com.enterprise.domain.mapper;

import com.enterprise.domain.dto.OrderDto;
import com.enterprise.domain.dto.OrderItemDto;
import com.enterprise.domain.entity.Order;
import com.enterprise.domain.entity.OrderItem;
import org.mapstruct.*;

/**
 * MapStruct mapper for Order ↔ OrderDto and OrderItem ↔ OrderItemDto.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    OrderDto toDto(Order order);

    @Mapping(source = "product.id",    target = "productId")
    @Mapping(source = "subtotal", target = "subtotal") // @Mapping(source = "getSubtotal()", target = "subtotal")
    OrderItemDto toItemDto(OrderItem item);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "user",        ignore = true)
    @Mapping(target = "items",       ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    @Mapping(target = "version",     ignore = true)
    Order toEntity(OrderDto dto);
}
