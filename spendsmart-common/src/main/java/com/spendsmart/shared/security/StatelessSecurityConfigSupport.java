package com.spendsmart.shared.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;
import java.util.stream.Stream;

public final class StatelessSecurityConfigSupport {

    public static final String[] STANDARD_PUBLIC_URLS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error",
            "/actuator/**"
    };

    private StatelessSecurityConfigSupport() {
    }

    public static SecurityFilterChain standardJwtChain(HttpSecurity http, OncePerRequestFilter jwtFilter) throws Exception {
        return jwtChain(http, jwtFilter, STANDARD_PUBLIC_URLS);
    }

    public static SecurityFilterChain jwtChain(HttpSecurity http, OncePerRequestFilter jwtFilter, String... publicUrls) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new CookieCsrfTokenRepository())
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(publicUrls)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicUrls).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    public static String[] mergePublicUrls(String... extraPublicUrls) {
        return Stream.concat(Arrays.stream(STANDARD_PUBLIC_URLS), Arrays.stream(extraPublicUrls))
                .toArray(String[]::new);
    }
}
