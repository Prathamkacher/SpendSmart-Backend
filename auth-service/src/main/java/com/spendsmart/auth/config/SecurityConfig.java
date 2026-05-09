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
import com.spendsmart.shared.security.CsrfCookieFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

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
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            // Enable CSRF — fixed security hotspot
            .csrf(csrf -> csrf
                    .csrfTokenRepository(new CookieCsrfTokenRepository())
                    .csrfTokenRequestHandler(requestHandler)
                    .ignoringRequestMatchers(PUBLIC_URLS)
            )

            // CORS handled by Gateway
            .cors(AbstractHttpConfigurer::disable)

            // Stateless — no HTTP sessions
            .sessionManagement(session ->
            		session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            // Endpoint access rules
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_URLS).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // preflight
                    .anyRequest().authenticated()
            )

            // OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                    // Dedicated handler: creates/finds user in DB, issues proper JWT + refresh token
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
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new CsrfCookieFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
