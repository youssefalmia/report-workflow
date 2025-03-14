package com.youssef.reportworkflow.handlers;

import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.exception.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;

/**
 * @author Jozef
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReportNotFound(ReportNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedUserException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(InvalidReportStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidReportStateException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                message,
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURI(),
                Instant.now()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}
