package com.spendsmart.expense.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI expenseServiceOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SpendSmart — Expense Service API")
                        .description("Expense management microservice for the SpendSmart platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SpendSmart Team")
                                .email("support@spendsmart.app")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
