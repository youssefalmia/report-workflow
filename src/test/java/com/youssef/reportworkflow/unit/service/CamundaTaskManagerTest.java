package com.youssef.reportworkflow.unit.service;

import com.youssef.reportworkflow.service.camunda.*;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.runtime.*;
import org.camunda.bpm.engine.task.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Jozef
 */
@ExtendWith(MockitoExtension.class)
class CamundaTaskManagerTest {

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private CamundaTaskManager camundaTaskManager;

    private final String processKey = "reportWorkflow";
    private final String businessKey = "report-123";
    private final Long reportId = 1L;
    private final String taskKey = "reviewTask";
    private final Map<String, Object> variables = Map.of("reportId", reportId);

    @Test
    void startProcessInstance_ShouldStartAndReturnProcessId() {
        // Given a mocked process instance
        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn("mock-process-id");

        // When the process is started
        when(runtimeService.startProcessInstanceByKey(processKey, businessKey, variables))
                .thenReturn(mockInstance);

        String processInstanceId = camundaTaskManager.startProcessInstance(processKey, businessKey, variables);

        // Then ensure correct delegation and output
        verify(runtimeService).startProcessInstanceByKey(processKey, businessKey, variables);
        assertEquals("mock-process-id", processInstanceId);
    }

    @Test
    void findTaskByDefinition_ShouldReturnTaskWhenExists() {
        // Given a mocked task
        Task mockTask = mock(Task.class);
        TaskQuery taskQuery = mock(TaskQuery.class);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processVariableValueEquals("reportId", reportId)).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey(taskKey)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);


        // When searching for a task
        Optional<Task> result = camundaTaskManager.findTaskByDefinition(reportId, taskKey);

        // Then ensure task is found
        verify(taskService).createTaskQuery();
        assertTrue(result.isPresent());
        assertEquals(mockTask, result.get());
    }

    @Test
    void findTaskByDefinition_ShouldReturnEmptyIfNotFound() {
        // Given no task exists
        // by default, mocked methods return null
        TaskQuery taskQuery = mock(TaskQuery.class);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processVariableValueEquals("reportId", reportId)).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey(taskKey)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null);

        // When searching for a task
        Optional<Task> result = camundaTaskManager.findTaskByDefinition(reportId, taskKey);

        // Then ensure empty result
        assertTrue(result.isEmpty());
    }

    @Test
    void completeTask_ShouldCompleteTask() {
        // Given a mocked task
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn("mock-task-id");

        // When completing the task
        camundaTaskManager.completeTask(mockTask, variables);

        // Then ensure task completion is triggered
        verify(taskService).complete("mock-task-id", variables);
    }

    @Test
    void getActiveTask_ShouldReturnActiveTaskIfExists() {
        // Given a mocked active task
        TaskQuery mockTaskQuery = mock(TaskQuery.class);
        Task activeTask = mock(Task.class);
        when(taskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.processVariableValueEquals("reportId", reportId)).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(activeTask);

        // When fetching active task
        Optional<Task> result = camundaTaskManager.getActiveTask(reportId);

        // Then: Ensure task retrieval
        verify(taskService).createTaskQuery();
        assertTrue(result.isPresent());
        assertEquals(activeTask, result.get());
    }

    @Test
    void getActiveTask_ShouldReturnEmptyIfNoActiveTask() {
        // Given mock TaskQuery
        TaskQuery mockTaskQuery = mock(TaskQuery.class);

        // Stub the TaskQuery chain
        when(taskService.createTaskQuery()).thenReturn(mockTaskQuery);
        when(mockTaskQuery.processVariableValueEquals("reportId", reportId)).thenReturn(mockTaskQuery);
        when(mockTaskQuery.singleResult()).thenReturn(null); // Simulating no task found

        // When fetching active task
        Optional<Task> result = camundaTaskManager.getActiveTask(reportId);

        // Then ensure empty response
        assertTrue(result.isEmpty());
    }
}

