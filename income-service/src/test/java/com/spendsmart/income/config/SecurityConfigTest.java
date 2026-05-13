package com.spendsmart.income.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SecurityConfigTest.TestApp.class)
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void securityFilterChainBean_ShouldLoad() {
        assertThat(securityFilterChain).isNotNull();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
            org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class,
            org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
    })
    @Import(SecurityConfig.class)
    static class TestApp {
    }
}
