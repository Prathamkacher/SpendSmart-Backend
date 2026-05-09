package com.spendsmart.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void openApi_ShouldExposeBearerSchemeAndMetadata() {
        SwaggerConfig swaggerConfig = new SwaggerConfig();

        OpenAPI openAPI = swaggerConfig.openAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("SpendSmart Auth Service API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("BearerAuth");
        assertThat(openAPI.getSecurity()).hasSize(1);
    }
}
