package com.youssef.reportworkflow.service.camunda;

import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.dto.*;
import lombok.*;
import org.camunda.bpm.engine.delegate.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

/**
 * @author Jozef
 */
@Component
@RequiredArgsConstructor
public class ReportStateListener implements TaskListener {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notify(DelegateTask delegateTask) {
        Long reportId = (Long) delegateTask.getVariable("reportId");

        ReportState newState;
        switch (delegateTask.getTaskDefinitionKey()) {
            case "createTask":
                newState = ReportState.CREATED;
                break;
            case "reviewTask":
                newState = ReportState.REVIEWED;
                break;
            case "validateTask":
                newState = ReportState.VALIDATED;
                break;
            case "refuseTask":
                newState = ReportState.REFUSED;
                break;
            default:
                return; // No update needed
        }

        // Emit an event instead of directly modifying the database
        eventPublisher.publishEvent(new ReportStateChangedEvent(this, reportId, newState));
    }
}

