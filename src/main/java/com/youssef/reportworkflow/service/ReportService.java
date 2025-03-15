package com.youssef.reportworkflow.service;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.exception.*;
import com.youssef.reportworkflow.mapper.*;
import lombok.*;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;

import java.time.*;

/**
 * @author Jozef
 */
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportWorkflowService workflowService;
    private final ReportMapper reportMapper;
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public ReportDTO startReportWorkflow(String title, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(UserNotFoundException::new);

        Report report = new Report();
        report.setTitle(title);
        report.setOwner(owner);

        Report savedReport = reportRepository.save(report);

        // rollback mechanism if the workflow fails
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                // Start workflow, workflowService manages the state using BPM
                workflowService.startWorkflow(savedReport.getId(), ownerId);
            }
        });


        return new ReportDTO(report.getId(), report.getTitle(), owner.getUsername(), ReportState.CREATED);
    }

    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public void confirmReportCreation(Long reportId, Long ownerId) {
        workflowService.createReport(reportId, ownerId);
    }


    @Transactional
    @PreAuthorize("hasRole('REVIEWER')")
    public ReportDTO reviewReport(Long reportId, Long reviewerId) {
        Report report = getReportById(reportId);

        // Ensure the state is valid using BPMN instead of database
        validateReportState(reportId, ReportState.CREATED);

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(UserNotFoundException::new);

        if (!reviewer.getRoles().contains(Role.REVIEWER)) {
            throw new ReviewerPermissionException();
        }

        // Call workflow service to transition the state
        workflowService.reviewReport(reportId, reviewerId);

        return reportMapper.toDTO(report);
    }

    @Transactional
    @PreAuthorize("hasRole('VALIDATOR')")
    public ReportDTO validateOrRefuseReport(Long reportId, Long validatorId, boolean isApproved) {
        Report report = getReportById(reportId);

        // Ensure the state is valid using BPMN instead of database
        validateReportState(reportId, ReportState.REVIEWED);

        User validator = userRepository.findById(validatorId)
                .orElseThrow(UserNotFoundException::new);

        if (!validator.getRoles().contains(Role.VALIDATOR)) {
            throw new ValidatorPermissionException();
        }

        // Delegate decision processing to workflow service
        workflowService.processValidationDecision(reportId, validatorId, isApproved);

        return reportMapper.toDTO(report);
    }

    /**
     * Fetches the report and updates its state from the workflow engine.
     */
    private Report getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

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
            throw new InvalidReportStateException(expectedState,currentState);
        }
    }
}
