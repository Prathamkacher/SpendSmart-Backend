package com.spendsmart.category.config;

import com.spendsmart.shared.security.AbstractJwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService extends AbstractJwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected String getJwtSecret() {
        return jwtSecret;
    }
}
