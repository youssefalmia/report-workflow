package com.youssef.reportworkflow.dto;

import lombok.*;

import java.time.*;
import java.time.format.*;

/**
 * @author Jozef
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String message;
    private final int statusCode;
    private final String error;
    private final String path;
    private final String timestamp;

    public ErrorResponse(String message, int statusCode, String error, String path, Instant timestamp) {
        this.message = message;
        this.statusCode = statusCode;
        this.error = error;
        this.path = path;
        this.timestamp = DateTimeFormatter.ISO_INSTANT.format(timestamp); // Avoid object mapper error on Instant
    }
}
