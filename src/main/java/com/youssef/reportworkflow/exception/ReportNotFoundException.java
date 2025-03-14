package com.youssef.reportworkflow.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jozef
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(Long reportId) {
        super("Report with ID " + reportId + " not found");
    }
}
