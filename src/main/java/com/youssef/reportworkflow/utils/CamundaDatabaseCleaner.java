package com.youssef.reportworkflow.utils;

import lombok.*;
import org.camunda.bpm.engine.*;
import org.springframework.stereotype.*;

/**
 * @author Jozef
 */
@Service
@RequiredArgsConstructor
public class CamundaDatabaseCleaner {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;

    public void cleanAllData() {
        // Delete all running process instances
        runtimeService.createProcessInstanceQuery()
                .list()
                .forEach(processInstance ->
                        runtimeService.deleteProcessInstance(processInstance.getId(), "Cleaning database")
                );

        // Delete all history
        historyService.createHistoricProcessInstanceQuery()
                .list()
                .forEach(historicProcessInstance ->
                        historyService.deleteHistoricProcessInstance(historicProcessInstance.getId())
                );

        // Delete all deployments (process definitions)
        repositoryService.createDeploymentQuery()
                .list()
                .forEach(deployment ->
                        repositoryService.deleteDeployment(deployment.getId(), true) // true -> cascade delete
                );

        System.out.println("Camunda database cleaned successfully.");
    }
}

