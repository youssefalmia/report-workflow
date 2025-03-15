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

    private final IReportWorkflowStrategy workflowStrategy;

    @Autowired
    public ReportWorkflowService(ReportWorkflowFactory workflowFactory,
                                 @Value("${workflow.engine}") String engineType) {
        // cache workflowStrategy at startup so that we access it directly on each method ( no map lookups )
        this.workflowStrategy = workflowFactory.getWorkflowStrategy(engineType);
    }

    public String startWorkflow(Long reportId, Long ownerId) {
        return workflowStrategy.startWorkflow(reportId, ownerId);
    }

    public void createReport(Long reportId, Long ownerId) {
        workflowStrategy.createReport(reportId, ownerId);
    }

    public void reviewReport(Long reportId, Long reviewerId) {
        workflowStrategy.reviewReport(reportId, reviewerId);
    }

    public void processValidationDecision(Long reportId, Long validatorId, boolean isApproved) {
        workflowStrategy.processValidationDecision(reportId, validatorId, isApproved);
    }

    public ReportState getReportState(Long reportId) {
        return workflowStrategy.getReportState(reportId);
    }

}

