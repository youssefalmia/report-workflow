package com.youssef.reportworkflow.service.camunda;

import lombok.*;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.task.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * @author Jozef
 * this class handles all camunda task related stuff
 * (e.g., starting process, getting tasks, completing tasks etc. )
 */
@Service
@RequiredArgsConstructor
public class CamundaTaskManager {
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    // we will need a method to start the process by it's key
    // a method to get a task by reportId and key ( createTask, reviewTask etc. )
    // a method that completes a certain task

    public String startProcessInstance(String processKey, String businessKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processKey, businessKey, variables).getId();
    }

    public Optional<Task> findTaskByDefinition(Long reportId, String taskKey) {
        return Optional.ofNullable(taskService.createTaskQuery()
                .processVariableValueEquals("reportId", reportId)
                .taskDefinitionKey(taskKey)
                .singleResult());
    }

    public void completeTask(Task task, Map<String, Object> variables) {
        taskService.complete(task.getId(), variables);
    }

    public Optional<Task> getActiveTask(Long reportId) {
        return Optional.ofNullable(taskService.createTaskQuery()
                .processVariableValueEquals("reportId", reportId)
                .singleResult());
    }

}
