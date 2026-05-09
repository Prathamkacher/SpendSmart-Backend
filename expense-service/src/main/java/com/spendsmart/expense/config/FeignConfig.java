package com.spendsmart.expense.config;

import com.spendsmart.expense.constants.AppConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign configuration that forwards the JWT Authorization header
 * from the incoming request to outbound Feign calls.
 * This ensures Budget-Service can authenticate the calling user.
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor authorizationForwardInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                String authHeader = attributes.getRequest().getHeader(AppConstants.AUTH_HEADER);
                if (authHeader != null && authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
                    template.header(AppConstants.AUTH_HEADER, authHeader);
                }
            }
        };
    }
}
