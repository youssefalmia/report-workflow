package com.youssef.reportworkflow.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jozef
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ReviewerPermissionException extends RuntimeException {
    public ReviewerPermissionException() {
        super("Only Reviewers can change the state from 'Created' to 'Reviewed'.");
    }
    public ReviewerPermissionException(String message) {
        super(message);
    }
}
