package com.spendsmart.income;

import com.spendsmart.shared.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class SupportClassesTest {

    @Test
    void main_ShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            IncomeServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});

            springApplication.verify(() ->
                    SpringApplication.run(IncomeServiceApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }

    @Test
    void apiResponseFactoriesAndConstructors_ShouldPopulateExpectedValues() {
        ApiResponse<String> success = ApiResponse.success("saved", "income");
        ApiResponse<Void> error = ApiResponse.error("failed");
        assertThat(success.isSuccess()).isTrue();
        assertThat(success.getData()).isEqualTo("income");
        assertThat(success.getTimestamp()).isNotNull();
        assertThat(error.isSuccess()).isFalse();
        assertThat(error.getMessage()).isEqualTo("failed");
    }
}
