package com.spendsmart.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = StatelessSecurityConfigSupportTest.TestApp.class)
@AutoConfigureMockMvc
class StatelessSecurityConfigSupportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void mergePublicUrlsShouldAppendExtraUrls() {
        String[] merged = StatelessSecurityConfigSupport.mergePublicUrls("/public", "/health");

        assertThat(merged).contains("/public", "/health");
        assertThat(Arrays.asList(merged)).contains(StatelessSecurityConfigSupport.STANDARD_PUBLIC_URLS);
    }

    @Test
    void standardJwtChainShouldCreateFilterChainBean() {
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    void publicUrlShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public"))
                .andExpect(status().isOk());
    }

    @Test
    void privateUrlShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/private"))
                .andExpect(status().isForbidden());
    }

    @Test
    void optionsRequestsShouldBePermitted() throws Exception {
        mockMvc.perform(options("/private"))
                .andExpect(status().isOk());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(TestSecurityConfiguration.class)
    static class TestApp {
    }

    @Configuration
    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http)
                throws Exception {
            return StatelessSecurityConfigSupport.jwtChain(http, jwtAuthenticationFilter(), "/public");
        }

        @Bean
        OncePerRequestFilter jwtAuthenticationFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }

        @RestController
        @RequestMapping
        static class TestController {
            @GetMapping("/public")
            ResponseEntity<String> publicEndpoint() {
                return ResponseEntity.ok("public");
            }

            @GetMapping("/private")
            ResponseEntity<String> privateEndpoint() {
                return ResponseEntity.ok("private");
            }
        }
    }
}
