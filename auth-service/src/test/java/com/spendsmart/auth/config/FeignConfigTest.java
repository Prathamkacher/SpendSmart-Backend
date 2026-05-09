package com.spendsmart.auth.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void requestInterceptor_ShouldPropagateAuthorizationHeader() {
        FeignConfig feignConfig = new FeignConfig();
        RequestInterceptor interceptor = feignConfig.requestInterceptor();
        RequestTemplate template = new RequestTemplate();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        interceptor.apply(template);

        assertThat(template.headers()).containsKey("Authorization");
        assertThat(template.headers().get("Authorization")).containsExactly("Bearer token-123");
    }

    @Test
    void requestInterceptor_ShouldDoNothingWhenNoRequestContextExists() {
        FeignConfig feignConfig = new FeignConfig();
        RequestInterceptor interceptor = feignConfig.requestInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("Authorization");
    }
}
