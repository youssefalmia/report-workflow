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
public class ReportStateListener implements TaskListener, ExecutionListener {
    private final ApplicationEventPublisher eventPublisher;

    // for user tasks
    @Override
    public void notify(DelegateTask delegateTask) {
        Long reportId = (Long) delegateTask.getVariable("reportId");
        Long userId = (Long) delegateTask.getVariable("userId");
        ReportState newState = switch (delegateTask.getTaskDefinitionKey()) {
            case "createTask" -> ReportState.CREATED;
            case "reviewTask" -> ReportState.REVIEWED;
            default -> null;
        };

        if (newState != null) {
            eventPublisher.publishEvent(new ReportStateChangedEvent(this, reportId, userId, newState));
        }
    }

    // for event execution such as the two end events
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Long reportId = (Long) execution.getVariable("reportId");
        Long userId = (Long) execution.getVariable("userId");

        ReportState newState = switch (execution.getCurrentActivityId()) {
            case "endValidated" -> ReportState.VALIDATED;
            case "endRefused" -> ReportState.REFUSED;
            default -> null;
        };

        if (newState != null) {
            eventPublisher.publishEvent(new ReportStateChangedEvent(this, reportId, userId, newState));
        }
    }
}

