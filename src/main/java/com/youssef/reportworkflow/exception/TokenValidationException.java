package com.youssef.reportworkflow.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jozef
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenValidationException extends RuntimeException {
    public TokenValidationException(){
        super("Invalid token");
    }
    public TokenValidationException(String message){
        super(message);
    }
    public TokenValidationException(String message,Exception e){
        super(message,e);
    }
}
