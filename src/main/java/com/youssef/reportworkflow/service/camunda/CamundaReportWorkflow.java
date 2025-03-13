package com.youssef.reportworkflow.service.camunda;

import com.youssef.reportworkflow.domain.enums.*;
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
public class CamundaReportWorkflow implements IReportWorkflowStrategy {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Override
    public String startWorkflow(Long reportId, Long ownerId) {
        ProcessInstance existingProcess = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(reportId.toString())  // Use Business Key instead
                .singleResult();

        if (existingProcess != null) {
            // Return existing process ID instead of throwing an error
            return existingProcess.getId();
        }

        // No existing process, start a new one
        Map<String, Object> variables = new HashMap<>();
        variables.put("reportId", reportId);
        variables.put("ownerId", ownerId);

        return runtimeService.startProcessInstanceByKey("reportWorkflow", reportId.toString(), variables).getId();
    }

    @Override
    public void createReport(Long reportId, Long ownerId) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("reportId", reportId)
                .taskDefinitionKey("createTask")
                .singleResult();

        if (task != null) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("ownerId", ownerId);
            taskService.complete(task.getId(), variables);
        } else {
            throw new IllegalStateException("No active 'createTask' found for report: " + reportId);
        }
    }

    @Override
    public void reviewReport(Long reportId, Long reviewerId) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("reportId", reportId)
                .taskDefinitionKey("reviewTask")
                .singleResult();

        if (task != null) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("reviewerId", reviewerId);
            taskService.complete(task.getId(), variables);
        } else {
            throw new IllegalStateException("No active 'reviewTask' found for report: " + reportId);
        }
    }

    @Override
    public void processValidationDecision(Long reportId, Long validatorId, boolean isApproved) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("reportId", reportId)
                .taskDefinitionKey("validateTask")
                .singleResult();

        if (task != null) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("validatorId", validatorId);
            variables.put("isApproved", isApproved);
            taskService.complete(task.getId(), variables);
        } else {
            throw new IllegalStateException("No active 'validateTask' found for report: " + reportId);
        }
    }

    @Override
    public ReportState getReportState(Long reportId) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("reportId", reportId)
                .singleResult();

        if (task == null) {
            return null;
        }
        System.out.println("processInstance: ");
        System.out.println(task);
        System.out.println(task.getTaskDefinitionKey());
        String taskDefinitionKey = task.getTaskDefinitionKey();  // This correctly identifies the task

        return switch (taskDefinitionKey) {
            case "createTask" -> ReportState.CREATED;
            case "reviewTask" -> ReportState.CREATED;
            case "validateTask" -> ReportState.REVIEWED;  // Review & Validation are similar
            case "endValidated" -> ReportState.VALIDATED;
            case "endRefused" -> ReportState.REFUSED;
            default -> ReportState.CREATED;
        };
    }


}
