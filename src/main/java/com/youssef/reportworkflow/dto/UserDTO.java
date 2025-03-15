package com.youssef.reportworkflow.dto;

import com.youssef.reportworkflow.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

/**
 * @author Jozef
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private Set<Role> roles = new HashSet<>();
}

