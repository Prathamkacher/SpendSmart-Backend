package com.spendsmart.notification.security;

import com.spendsmart.shared.security.AbstractJwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService extends AbstractJwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String extractUsername(String token) {
        return extractEmail(token);
    }

    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    @Override
    protected String getJwtSecret() {
        return secretKey;
    }
}
