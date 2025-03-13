package com.youssef.reportworkflow.service;

import com.youssef.reportworkflow.domain.enums.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

/**
 * @author Jozef
 */
@Service
public class ReportWorkflowService {

    private final ReportWorkflowFactory workflowFactory;
    private final String engineType;

    @Autowired
    public ReportWorkflowService(ReportWorkflowFactory workflowFactory,
                                 @Value("${workflow.engine}") String engineType) {
        this.workflowFactory = workflowFactory;
        this.engineType = engineType;
    }

    public String startWorkflow(Long reportId, Long ownerId) {
        return workflowFactory.getWorkflowStrategy(engineType).startWorkflow(reportId, ownerId);
    }

    public void createReport(Long reportId, Long ownerId) {
        workflowFactory.getWorkflowStrategy(engineType).createReport(reportId, ownerId);
    }

    public void reviewReport(Long reportId, Long reviewerId) {
        workflowFactory.getWorkflowStrategy(engineType).reviewReport(reportId, reviewerId);
    }

    public void processValidationDecision(Long reportId, Long validatorId, boolean isApproved) {
        workflowFactory.getWorkflowStrategy(engineType).processValidationDecision(reportId, validatorId, isApproved);
    }
    public ReportState getReportState(Long reportId) {
        return workflowFactory.getWorkflowStrategy(engineType).getReportState(reportId);
    }

}

