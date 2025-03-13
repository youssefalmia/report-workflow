package com.youssef.reportworkflow.service;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.exception.*;
import lombok.*;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.time.*;

/**
 * @author Jozef
 */
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportTransitionLogRepository transitionLogRepository;
    private final ReportWorkflowService workflowService;

    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public Report startReportWorkflow(String title, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        Report report = new Report();
        report.setTitle(title);
        report.setOwner(owner);

        Report savedReport = reportRepository.save(report);

        // Start workflow, workflowService manages the state using BPM
        workflowService.startWorkflow(savedReport.getId(), ownerId);

        return savedReport;
    }

    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public void confirmReportCreation(Long reportId, Long ownerId) {
        Report report = getReportById(reportId);

        workflowService.createReport(reportId, ownerId);

        transitionLogRepository.save(new ReportTransitionLog(report, report.getOwner(), ReportState.CREATED));
    }


    @Transactional
    @PreAuthorize("hasRole('REVIEWER')")
    public Report reviewReport(Long reportId, Long reviewerId) {
        Report report = getReportById(reportId);

        // Ensure the state is valid using BPMN instead of database
        validateReportState(reportId, ReportState.CREATED);

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

        if (!reviewer.getRoles().contains(Role.REVIEWER)) {
            throw new UnauthorizedUserException();
        }

        report.setReviewer(reviewer);
        transitionLogRepository.save(new ReportTransitionLog(report, reviewer, ReportState.REVIEWED));

        // Call workflow service to transition the state
        workflowService.reviewReport(reportId, reviewerId);

        return report;
    }

    @Transactional
    @PreAuthorize("hasRole('VALIDATOR')")
    public Report validateOrRefuseReport(Long reportId, Long validatorId, boolean isApproved) {
        Report report = getReportById(reportId);

        // Ensure the state is valid using BPMN instead of database
        validateReportState(reportId, ReportState.REVIEWED);

        User validator = userRepository.findById(validatorId)
                .orElseThrow(() -> new IllegalArgumentException("Validator not found"));

        if (!validator.getRoles().contains(Role.VALIDATOR)) {
            throw new UnauthorizedUserException();
        }

        transitionLogRepository.save(new ReportTransitionLog(report, validator, isApproved ? ReportState.VALIDATED : ReportState.REFUSED));

        // Delegate decision processing to workflow service
        workflowService.processValidationDecision(reportId, validatorId, isApproved);

        return report;
    }

    /**
     * Fetches the report and updates its state from the workflow engine.
     */
    private Report getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Fetch latest state from BPMN
        ReportState workflowState = workflowService.getReportState(reportId);

        if (workflowState != null) {
            report.setState(workflowState);
        }

        return report;
    }

    /**
     * Ensures report state is valid before executing a workflow action.
     */
    private void validateReportState(Long reportId, ReportState expectedState) {
        ReportState currentState = workflowService.getReportState(reportId);
        if (currentState != expectedState) {
            throw new IllegalStateException(
                    "Invalid state transition. Expected: " + expectedState + ", but was: " + currentState
            );
        }
    }
}
