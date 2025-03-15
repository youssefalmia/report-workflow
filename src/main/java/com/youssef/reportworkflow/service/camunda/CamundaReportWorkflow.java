package com.youssef.reportworkflow.service.camunda;

import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.exception.*;
import com.youssef.reportworkflow.service.*;
import lombok.*;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.engine.task.*;
import org.camunda.bpm.engine.variable.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * @author Jozef
 */
@Service
@Primary
@RequiredArgsConstructor
public class CamundaReportWorkflow implements IReportWorkflowStrategy {

    private final CamundaTaskManager camundaTaskManager;

    @Override
    public String startWorkflow(Long reportId, Long ownerId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("reportId", reportId);
        variables.put("ownerId", ownerId);

        return camundaTaskManager.startProcessInstance("reportWorkflow", reportId.toString(), variables);
    }

    @Override
    public void createReport(Long reportId, Long ownerId) {
        camundaTaskManager.findTaskByDefinition(reportId, "createTask")
                .ifPresentOrElse(
                        task -> {
                            Map<String, Object> variables = new HashMap<>();
                            variables.put("userId", ownerId);
                            camundaTaskManager.completeTask(task, variables);
                        },
                        () -> {
                            throw new NoActiveTaskForReportException("createTask", reportId.toString());
                        }
                );
    }

    @Override
    public void reviewReport(Long reportId, Long reviewerId) {
        camundaTaskManager.findTaskByDefinition(reportId, "reviewTask")
                .ifPresentOrElse(
                        task -> {
                            Map<String, Object> variables = new HashMap<>();
                            variables.put("userId", reviewerId);
                            camundaTaskManager.completeTask(task, variables);
                        },
                        () -> { throw new NoActiveTaskForReportException("reviewTask", reportId.toString()); }
                );
    }

    @Override
    public void processValidationDecision(Long reportId, Long validatorId, boolean isApproved) {
        camundaTaskManager.findTaskByDefinition(reportId, "validateTask")
                .ifPresentOrElse(
                        task -> {
                            Map<String, Object> variables = new HashMap<>();
                            variables.put("userId", validatorId);
                            variables.put("isApproved", isApproved);
                            camundaTaskManager.completeTask(task, variables);
                        },
                        () -> { throw new NoActiveTaskForReportException("validateTask", reportId.toString()); }
                );
    }

    @Override
    public ReportState getReportState(Long reportId) {
        return camundaTaskManager.getActiveTask(reportId)
                .map(task -> switch (task.getTaskDefinitionKey()) {
                    case "createTask" -> ReportState.CREATED;
                    case "reviewTask" -> ReportState.CREATED;
                    case "validateTask" -> ReportState.REVIEWED;
                    case "endValidated" -> ReportState.VALIDATED;
                    case "endRefused" -> ReportState.REFUSED;
                    default -> ReportState.CREATED;
                })
                .orElse(null);
    }


}
