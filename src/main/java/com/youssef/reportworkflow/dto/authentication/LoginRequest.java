package com.youssef.reportworkflow.dto.authentication;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * @author Jozef
 */
@AllArgsConstructor
@Getter
@Setter
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
