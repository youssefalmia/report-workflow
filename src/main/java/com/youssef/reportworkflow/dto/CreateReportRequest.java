package com.youssef.reportworkflow.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * @author Jozef
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {
    @NotBlank(message = "Title is required")
    private String title;

}
