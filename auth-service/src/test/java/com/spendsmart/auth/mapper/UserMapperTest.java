package com.spendsmart.auth.mapper;

import com.spendsmart.auth.dto.UserProfileResponse;
import com.spendsmart.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toProfileResponse_ShouldMapExpectedFields() {
        User user = User.builder()
                .userId(9L)
                .fullName("Alex Doe")
                .email("alex@example.com")
                .currency("USD")
                .timezone("UTC")
                .bio("Hello")
                .monthlyBudget(new BigDecimal("2500.00"))
                .planType(User.PlanType.PRO)
                .role(User.Role.ADMIN)
                .provider(User.AuthProvider.GOOGLE)
                .build();

        UserProfileResponse response = userMapper.toProfileResponse(user);

        assertThat(response.getUserId()).isEqualTo(9L);
        assertThat(response.getFullName()).isEqualTo("Alex Doe");
        assertThat(response.getEmail()).isEqualTo("alex@example.com");
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getTimezone()).isEqualTo("UTC");
        assertThat(response.getBio()).isEqualTo("Hello");
        assertThat(response.getMonthlyBudget()).isEqualByComparingTo("2500.00");
        assertThat(response.getPlanType()).isEqualTo(User.PlanType.PRO);
        assertThat(response.getRole()).isEqualTo(User.Role.ADMIN);
        assertThat(response.getProvider()).isEqualTo(User.AuthProvider.GOOGLE);
    }
}
