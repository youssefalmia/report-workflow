package com.youssef.reportworkflow.handlers;

import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.exception.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.*;
import java.time.*;

/**
 * @author Jozef
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReportNotFound(ReportNotFoundException ex, HttpServletRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedUserException ex, HttpServletRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(InvalidReportStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidReportStateException ex, HttpServletRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(TokenExpirationException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiration(TokenExpirationException ex, HttpServletRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleTokenValidation(TokenValidationException ex, HttpServletRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ValidatorPermissionException.class)
    public ResponseEntity<ErrorResponse> handleValidatorPermission(ValidatorPermissionException ex, HttpServletRequest request) {
        log.error("Validator permission error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(ReviewerPermissionException.class)
    public ResponseEntity<ErrorResponse> handleReviewerPermission(ReviewerPermissionException ex, HttpServletRequest request) {
        log.error("Reviewer permission error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(OwnerPermissionException.class)
    public ResponseEntity<ErrorResponse> handleOwnerPermission(OwnerPermissionException ex, HttpServletRequest request) {
        log.error("Owner permission error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
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
