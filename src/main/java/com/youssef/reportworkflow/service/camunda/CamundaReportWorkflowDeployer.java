package com.youssef.reportworkflow.service.camunda;

import jakarta.annotation.*;
import lombok.*;
import org.camunda.bpm.engine.*;
import org.springframework.stereotype.*;

/**
 * @author Jozef
 */
@Component
@RequiredArgsConstructor
public class CamundaReportWorkflowDeployer {

    private final RepositoryService repositoryService;

    @PostConstruct
    public void deploy() {
        repositoryService.createDeployment()
                .addClasspathResource("bpm/camunda/report-workflow.bpmn")
                .deploy();
    }
}

