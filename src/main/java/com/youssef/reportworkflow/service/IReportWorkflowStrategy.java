package com.youssef.reportworkflow.service;

import com.youssef.reportworkflow.domain.enums.*;

/**
 * @author Jozef
 */
public interface IReportWorkflowStrategy {
    String startWorkflow(Long reportId,Long ownerId); // should return the processId for BPM based solutions, create a custom one for non-bpm solutions
    void createReport(Long reportId,Long ownerId);
    void reviewReport(Long reportId,Long reviewerId);
    void processValidationDecision(Long reportId, Long validatorId, boolean isApproved);
    ReportState getReportState(Long reportId);
}
