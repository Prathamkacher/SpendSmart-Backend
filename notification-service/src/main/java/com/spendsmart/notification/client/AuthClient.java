package com.spendsmart.notification.client;

import com.spendsmart.notification.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthClient {

    @GetMapping("/internal/users/{id}")
    UserProfileResponse getUserById(@PathVariable("id") Long id);
}
