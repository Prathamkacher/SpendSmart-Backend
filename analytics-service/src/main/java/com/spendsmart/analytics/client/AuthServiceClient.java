package com.spendsmart.analytics.client;

import com.spendsmart.shared.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Feign client for communicating with the Auth service.
 * Used to fetch user information and ID lists for global analytics.
 */
@FeignClient(name = "auth-service", path = "/api")
public interface AuthServiceClient {

    @GetMapping("/auth/users/ids")
    ApiResponse<List<Long>> getAllUserIds();
}
