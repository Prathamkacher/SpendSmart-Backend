// com/spendsmart/auth/exception/AuthException.java
package com.spendsmart.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for authentication and authorization failures.
 * Returns a 401 Unauthorized HTTP status by default.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}