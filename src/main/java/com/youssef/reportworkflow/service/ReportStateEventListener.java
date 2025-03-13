package com.youssef.reportworkflow.service;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.dto.*;
import lombok.*;
import org.springframework.context.event.*;
import org.springframework.stereotype.*;

/**
 * @author Jozef
 */
@Component
@RequiredArgsConstructor
public class ReportStateEventListener {
    private final ReportRepository reportRepository;

    @EventListener
    public void onReportStateChanged(ReportStateChangedEvent event) {
        Report report = reportRepository.findById(event.getReportId())
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setState(event.getNewState());
        reportRepository.save(report);

        System.out.println("Report with ID: " + event.getReportId() + " updated to state: " + event.getNewState());
    }
}

