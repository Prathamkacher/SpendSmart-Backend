package com.spendsmart.recurring.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "12345678901234567890123456789012";

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtSecret", SECRET);
    }

    @Test
    void doFilter_ShouldPassThroughWhenGatewayAlreadySuppliesUserId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "44");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<ServletRequest> forwarded = new AtomicReference<>();

        filter.doFilter(request, response, capture(forwarded));

        assertThat(forwarded.get()).isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilter_ShouldInjectUserIdHeaderForValidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + buildToken(91L, true));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<ServletRequest> forwarded = new AtomicReference<>();

        filter.doFilter(request, response, capture(forwarded));

        HttpServletRequest wrapped = (HttpServletRequest) forwarded.get();
        assertThat(wrapped.getHeader("X-User-Id")).isEqualTo("91");
        assertThat(java.util.Collections.list(wrapped.getHeaders("X-User-Id"))).containsExactly("91");
        Enumeration<String> names = wrapped.getHeaderNames();
        assertThat(java.util.Collections.list(names)).contains("Authorization", "X-User-Id");
    }

    @Test
    void doFilter_ShouldRejectTokenWithoutUserIdClaim() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + buildToken(null, true));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("Chain should not continue");
        });

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilter_ShouldRejectInvalidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("Chain should not continue");
        });

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilter_ShouldPassThroughWhenNoAuthHeaderExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<ServletRequest> forwarded = new AtomicReference<>();

        filter.doFilter(request, response, capture(forwarded));

        assertThat(forwarded.get()).isSameAs(request);
    }

    private FilterChain capture(AtomicReference<ServletRequest> forwarded) {
        return (ServletRequest request, ServletResponse response) -> forwarded.set(request);
    }

    private String buildToken(Long userId, boolean valid) {
        var builder = Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis() - 2_000))
                .setExpiration(new Date(System.currentTimeMillis() + (valid ? 60_000 : -1_000)))
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes(StandardCharsets.UTF_8));
        if (userId != null) {
            builder.claim("userId", userId);
        }
        return builder.compact();
    }
}
