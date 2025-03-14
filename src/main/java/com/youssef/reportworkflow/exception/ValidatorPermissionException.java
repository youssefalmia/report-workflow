package com.youssef.reportworkflow.exception;

import com.youssef.reportworkflow.domain.enums.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jozef
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ValidatorPermissionException extends RuntimeException {
    public ValidatorPermissionException() {
        super("Only Validators can change the state from 'Reviewed' to 'Validated'.");
    }
    public ValidatorPermissionException(String message) {
        super(message);
    }
}
