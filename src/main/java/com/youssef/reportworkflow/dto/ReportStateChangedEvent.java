package com.youssef.reportworkflow.dto;

import com.youssef.reportworkflow.domain.enums.*;
import lombok.*;

/**
 * @author Jozef
 */
@Getter
@AllArgsConstructor
public class ReportStateChangedEvent {
    private final Object source;
    private final Long reportId;
    private final ReportState newState;
}

