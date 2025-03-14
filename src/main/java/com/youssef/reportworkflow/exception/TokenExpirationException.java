package com.youssef.reportworkflow.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jozef
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenExpirationException extends RuntimeException {
    public TokenExpirationException(){
        super("JWT Token has expired");
    }
}
