package com.youssef.reportworkflow.exception;

import com.youssef.reportworkflow.domain.enums.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jozef
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidReportStateException extends RuntimeException {
    public InvalidReportStateException(ReportState expected, ReportState actual) {
        super("Invalid state transition. Expected: " + expected + ", but was: " + actual);
    }
    public InvalidReportStateException(String message) {
        super(message);
    }
}
