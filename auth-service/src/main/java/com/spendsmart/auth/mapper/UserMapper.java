// com/spendsmart/auth/mapper/UserMapper.java
package com.spendsmart.auth.mapper;

import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper — compile-time generated, zero reflection overhead.
 * componentModel = "spring" → injects as a Spring bean.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    // Field names match exactly, so no @Mapping annotations needed
    UserProfileResponse toProfileResponse(User user);
}