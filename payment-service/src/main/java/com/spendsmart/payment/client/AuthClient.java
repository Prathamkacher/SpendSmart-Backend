package com.spendsmart.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PutMapping("/api/auth/profile/upgrade")
    void upgradeUserPlan(@RequestParam("userId") Long userId, 
                         @RequestParam("planType") String planType,
                         @RequestParam("durationMonths") Integer durationMonths);
}
