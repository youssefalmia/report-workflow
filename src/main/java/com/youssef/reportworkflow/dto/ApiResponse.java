package com.youssef.reportworkflow.dto;

import lombok.*;

/**
 * @author Jozef
 */
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;

    // Constructor for Success with Data
    public ApiResponse(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    // Constructor for Success with Data with default message
    public ApiResponse(T data) {
        this.success = true;
        this.message = "success";
        this.data = data;
    }

    // Constructor for Success Without Data
    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
        this.data = null;
    }
}
