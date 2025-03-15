package com.youssef.reportworkflow.dto.authentication;

import com.youssef.reportworkflow.dto.*;
import lombok.*;

/**
 * @author Jozef
 */
@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;
    private UserDTO userDTO;
}
