package com.youssef.reportworkflow.dto;

import com.youssef.reportworkflow.domain.enums.*;
import lombok.*;

/**
 * @author Jozef
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {
    private Long id;
    private String title;
    private String owner;
    private ReportState state;
}
