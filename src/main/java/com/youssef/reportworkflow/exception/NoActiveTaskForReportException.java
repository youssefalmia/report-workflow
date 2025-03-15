package com.youssef.reportworkflow.exception;

import com.youssef.reportworkflow.domain.enums.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jozef
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NoActiveTaskForReportException extends RuntimeException {
    public NoActiveTaskForReportException(String taskKey, String reportId) {
        super("No active " + taskKey + " found for report: " + reportId);
    }
}
