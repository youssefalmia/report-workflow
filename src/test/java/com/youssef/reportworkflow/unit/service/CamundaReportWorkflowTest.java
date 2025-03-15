package com.youssef.reportworkflow.unit.service;

import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.exception.*;
import com.youssef.reportworkflow.service.camunda.*;
import org.camunda.bpm.engine.task.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Jozef
 */
@ExtendWith(MockitoExtension.class)
class CamundaReportWorkflowTest {

    @Mock
    private CamundaTaskManager camundaTaskManager;

    @InjectMocks
    private CamundaReportWorkflow camundaReportWorkflow;

    private final Long reportId = 1L;
    private final Long userId = 100L;

    @Test
    void startWorkflow_ShouldCallCamundaTaskManager() {
        // Given: Expected process instance ID
        String expectedProcessInstanceId = "mock-instance-id";

        when(camundaTaskManager.startProcessInstance(anyString(), anyString(), anyMap()))
                .thenReturn(expectedProcessInstanceId);

        // When: Calling startWorkflow
        String result = camundaReportWorkflow.startWorkflow(reportId, userId);

        // Then: Verify interaction with CamundaTaskManager
        verify(camundaTaskManager).startProcessInstance(eq("reportWorkflow"), eq(reportId.toString()), anyMap());
        assertEquals(expectedProcessInstanceId, result);
    }

    @Test
    void createReport_ShouldCompleteTask_WhenTaskExists() {
        // Given: A mock task is found
        Task mockTask = mock(Task.class);
        when(camundaTaskManager.findTaskByDefinition(reportId, "createTask"))
                .thenReturn(Optional.of(mockTask));

        // When: Calling createReport
        camundaReportWorkflow.createReport(reportId, userId);

        // Then: Ensure task is completed
        verify(camundaTaskManager).completeTask(eq(mockTask), anyMap());
    }

    @Test
    void createReport_ShouldThrowException_WhenTaskNotFound() {
        // Given: No task found
        when(camundaTaskManager.findTaskByDefinition(reportId, "createTask"))
                .thenReturn(Optional.empty());

        // Expect exception
        assertThrows(NoActiveTaskForReportException.class,
                () -> camundaReportWorkflow.createReport(reportId, userId));

        // Verify no task completion attempt was made
        verify(camundaTaskManager, never()).completeTask(any(Task.class), anyMap());
    }

    @Test
    void reviewReport_ShouldCompleteTask_WhenTaskExists() {
        // Given: A mock task is found
        Task mockTask = mock(Task.class);
        when(camundaTaskManager.findTaskByDefinition(reportId, "reviewTask"))
                .thenReturn(Optional.of(mockTask));

        // When: Calling reviewReport
        camundaReportWorkflow.reviewReport(reportId, userId);

        // Then: Ensure task is completed
        verify(camundaTaskManager).completeTask(eq(mockTask), anyMap());
    }

    @Test
    void reviewReport_ShouldThrowException_WhenTaskNotFound() {
        // Given: No task found
        when(camundaTaskManager.findTaskByDefinition(reportId, "reviewTask"))
                .thenReturn(Optional.empty());

        // Expect exception
        assertThrows(NoActiveTaskForReportException.class,
                () -> camundaReportWorkflow.reviewReport(reportId, userId));

        verify(camundaTaskManager, never()).completeTask(any(Task.class), anyMap());
    }

    @Test
    void processValidationDecision_ShouldCompleteTask_WhenTaskExists() {
        // Given: A mock task is found
        Task mockTask = mock(Task.class);
        when(camundaTaskManager.findTaskByDefinition(reportId, "validateTask"))
                .thenReturn(Optional.of(mockTask));

        // When: Calling processValidationDecision
        camundaReportWorkflow.processValidationDecision(reportId, userId, true);

        // Then: Ensure task is completed
        verify(camundaTaskManager).completeTask(eq(mockTask), anyMap());
    }

    @Test
    void processValidationDecision_ShouldThrowException_WhenTaskNotFound() {
        // Given: No task found
        when(camundaTaskManager.findTaskByDefinition(reportId, "validateTask"))
                .thenReturn(Optional.empty());

        // Expect exception
        assertThrows(NoActiveTaskForReportException.class,
                () -> camundaReportWorkflow.processValidationDecision(reportId, userId, true));

        verify(camundaTaskManager, never()).completeTask(any(Task.class), anyMap());
    }

    @Test
    void getReportState_ShouldReturnCorrectState_WhenTaskExists() {
        // Given: A mock task with `reviewTask` definition key
        Task mockTask = mock(Task.class);
        when(mockTask.getTaskDefinitionKey()).thenReturn("reviewTask");
        when(camundaTaskManager.getActiveTask(reportId)).thenReturn(Optional.of(mockTask));

        // When: Fetching report state
        ReportState result = camundaReportWorkflow.getReportState(reportId);

        // Then: Verify the expected state
        assertEquals(ReportState.CREATED, result);
    }

    @Test
    void getReportState_ShouldReturnNull_WhenNoActiveTaskExists() {
        // Given: No active task
        when(camundaTaskManager.getActiveTask(reportId)).thenReturn(Optional.empty());

        // When: Fetching report state
        ReportState result = camundaReportWorkflow.getReportState(reportId);

        // Then: Verify it returns null
        assertNull(result);
    }
}

