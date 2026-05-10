// com/spendsmart/auth/config/SecurityConfig.java
package com.spendsmart.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter       jwtAuthFilter;
    private final OAuth2LoginSuccessHandler     oauth2LoginSuccessHandler;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    // Public endpoints — no JWT required
    private static final String[] PUBLIC_URLS = {
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/auth/forgot-password",
            "/auth/verify-otp",
            "/auth/reset-password",
            "/auth/oauth2/**",
            "/auth/profile/upgrade",
            "/auth/internal/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — this is a JWT-based stateless API.
            // The JWT in the Authorization header provides inherent CSRF protection
            // since it is NOT automatically sent by the browser (unlike cookies).
            .csrf(AbstractHttpConfigurer::disable)

            // CORS handled by Gateway
            .cors(AbstractHttpConfigurer::disable)

            // Sessions only for OAuth2 login flow
            .sessionManagement(session ->
            		session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            // Endpoint access rules
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_URLS).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // preflight
                    .requestMatchers("/auth/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )

            // OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                    .successHandler(oauth2LoginSuccessHandler)
                    .failureHandler((request, response, exception) ->
                            response.sendRedirect(frontendUrl + "/auth/login?error=oauth_failed")
                    )
            )
            .logout(logout -> logout
                    .logoutUrl("/auth/logout")
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.setStatus(200);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":true, \"message\":\"Logged out successfully\"}");
                    })
            )

            // JWT filter runs before Spring's username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
