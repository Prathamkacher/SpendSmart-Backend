package com.spendsmart.category.config;

import com.spendsmart.shared.security.AbstractJwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for handling JWT related operations.
 * Extends {@link AbstractJwtService} to provide the specific secret key for this microservice.
 */
@Service
public class JwtService extends AbstractJwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Retrieves the JWT secret key from application properties.
     * @return The secret key string.
     */
    @Override
    protected String getJwtSecret() {
        return jwtSecret;
    }
}
