package com.enterprise.domain.mapper;

import com.enterprise.domain.dto.UserDto;
import com.enterprise.domain.entity.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for User ↔ UserDto.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    UserDto toDto(User user);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "roles",        ignore = true)
    @Mapping(target = "orders",       ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    User toEntity(UserDto dto);
}
