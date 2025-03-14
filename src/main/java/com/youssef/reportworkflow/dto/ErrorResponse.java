package com.youssef.reportworkflow.dto;

import lombok.*;

import java.time.*;

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
    private final Instant timestamp;
}
