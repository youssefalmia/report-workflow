package com.youssef.reportworkflow.handlers;

import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;

/**
 * @author Jozef
 */
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(CustomException.class)
//    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, HttpServletRequest request) {
//        return buildErrorResponse(ex.getMessage(), ex.getStatus(), request);
//    }
//
//    @ExceptionHandler(EmailAlreadyUsedException.class)
//    public ResponseEntity<ErrorResponse> handleEmailAlreadyUsed(EmailAlreadyUsedException ex, HttpServletRequest request) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
//    }
//
//    @ExceptionHandler(PhoneNumberAlreadyUsedException.class)
//    public ResponseEntity<ErrorResponse> handlePhoneNumberAlreadyUsed(PhoneNumberAlreadyUsedException ex, HttpServletRequest request) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
//    }
//
//    @ExceptionHandler(VisitorNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleVisitorNotFound(VisitorNotFoundException ex, HttpServletRequest request) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
//    }
//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, request);
//    }
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
//    }
//
//    @ExceptionHandler(InvalidPasswordException.class)
//    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex, HttpServletRequest request) {
//        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
//    }
//
//    // Handle Unexpected Errors
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
//        ex.printStackTrace();
//        return buildErrorResponse("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR, request);
//    }
//
//    // Utility method to construct error responses
//    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
//        ErrorResponse errorResponse = new ErrorResponse(
//                message,
//                status.value(),
//                status.getReasonPhrase(),
//                request.getRequestURI(),
//                Instant.now()
//        );
//        return ResponseEntity.status(status).body(errorResponse);
//    }
//}
