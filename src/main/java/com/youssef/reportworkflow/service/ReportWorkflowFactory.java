package com.youssef.reportworkflow.service;

import com.youssef.reportworkflow.service.camunda.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Jozef
 */
@Component
public class ReportWorkflowFactory {

    private final Map<String, IReportWorkflowStrategy> strategies;

    @Autowired
    public ReportWorkflowFactory(List<IReportWorkflowStrategy> strategyList) {
        this.strategies = new HashMap<>();
        for (IReportWorkflowStrategy strategy : strategyList) {
            if (strategy instanceof CamundaReportWorkflow) {
                strategies.put("camunda", strategy);
            }
            // Add other workflow engines if applicable
        }
    }

    public IReportWorkflowStrategy getWorkflowStrategy(String engineType) {
        return strategies.get(engineType);
    }
}

