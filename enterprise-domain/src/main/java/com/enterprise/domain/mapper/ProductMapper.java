package com.enterprise.domain.mapper;

import com.enterprise.domain.dto.ProductDto;
import com.enterprise.domain.entity.Product;
import org.mapstruct.*;

/**
 * MapStruct mapper for Product ↔ ProductDto conversion.
 * Design Pattern: Mapper (separates transport layer from persistence).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(source = "category.id",   target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDto toDto(Product product);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "category",    ignore = true)
    @Mapping(target = "orderItems",  ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    @Mapping(target = "version",     ignore = true)
    Product toEntity(ProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "category",    ignore = true)
    @Mapping(target = "orderItems",  ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    @Mapping(target = "version",     ignore = true)
    void updateEntityFromDto(ProductDto dto, @MappingTarget Product product);
}
