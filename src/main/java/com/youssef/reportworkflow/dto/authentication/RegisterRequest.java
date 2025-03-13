package com.youssef.reportworkflow.dto.authentication;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * @author Jozef
 */
@AllArgsConstructor
@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
