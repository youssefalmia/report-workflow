package com.youssef.reportworkflow.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jozef
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class OwnerPermissionException extends RuntimeException {
    public OwnerPermissionException() {
        super("Only Owners can start the workflow and change state to 'Created'.");
    }
    public OwnerPermissionException(String message) {
        super(message);
    }
}
