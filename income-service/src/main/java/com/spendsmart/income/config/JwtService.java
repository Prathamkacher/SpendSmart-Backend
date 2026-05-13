package com.spendsmart.income.config;

import com.spendsmart.shared.security.AbstractJwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for extracting claims from JSON Web Tokens.
 * Extends {@link AbstractJwtService} to provide secret key management for the income service.
 */
@Service
public class JwtService extends AbstractJwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected String getJwtSecret() {
        return jwtSecret;
    }
}
