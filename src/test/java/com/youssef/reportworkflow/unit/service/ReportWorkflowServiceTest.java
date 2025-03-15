package com.youssef.reportworkflow.unit.service;

import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jozef
 */
@ExtendWith(MockitoExtension.class)
class ReportWorkflowServiceTest {
    @Mock
    private IReportWorkflowStrategy workflowStrategy;

    @Mock
    private ReportWorkflowFactory workflowFactory;

    private ReportWorkflowService reportWorkflowService;

    private final Long reportId = 1L;
    private final Long userId = 100L;
    private final boolean isApproved = true;

    @BeforeEach
    void setUp() {
        // Mock the factory to return the workflow strategy
        when(workflowFactory.getWorkflowStrategy(anyString())).thenReturn(workflowStrategy);

        // Manually instantiate the service
        reportWorkflowService = new ReportWorkflowService(workflowFactory, "camunda");
    }

    @Test
    void startWorkflow_ShouldCallStrategy() {
        when(workflowStrategy.startWorkflow(reportId, userId)).thenReturn("mock-process-id");

        // Call method
        String result = reportWorkflowService.startWorkflow(reportId, userId);

        // Verify delegation
        verify(workflowStrategy).startWorkflow(reportId, userId);

        // Ensure correct return value
        assertEquals("mock-process-id", result);
    }

    @Test
    void createReport_ShouldCallStrategy() {
        // Call method
        reportWorkflowService.createReport(reportId, userId);

        // Verify delegation
        verify(workflowStrategy).createReport(reportId, userId);
    }

    @Test
    void reviewReport_ShouldCallStrategy() {
        // Call method
        reportWorkflowService.reviewReport(reportId, userId);

        // Verify delegation
        verify(workflowStrategy).reviewReport(reportId, userId);
    }

    @Test
    void processValidationDecision_ShouldCallStrategy() {
        // Call method
        reportWorkflowService.processValidationDecision(reportId, userId, isApproved);

        // Verify delegation
        verify(workflowStrategy).processValidationDecision(reportId, userId, isApproved);
    }

    @Test
    void getReportState_ShouldCallStrategyAndReturnState() {
        when(workflowStrategy.getReportState(reportId)).thenReturn(ReportState.VALIDATED);

        // Call method
        ReportState result = reportWorkflowService.getReportState(reportId);

        // Verify delegation
        verify(workflowStrategy).getReportState(reportId);

        // Ensure correct return value
        assertEquals(ReportState.VALIDATED, result);
    }
}
