package com.youssef.reportworkflow.service;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.exception.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.context.event.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.concurrent.*;

/**
 * @author Jozef
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportStateEventListener {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportTransitionLogRepository transitionLogRepository;

    // this is the async equivalent of the method, so that we could handle high load
    @Async
    @EventListener
    @Transactional
    public void onReportStateChanged(ReportStateChangedEvent event) {

        Report report = reportRepository.findById(event.getReportId())
                .orElseThrow(() -> new ReportNotFoundException(event.getReportId()));

        User user =userRepository.findById(event.getUserId())
                        .orElseThrow(UserNotFoundException::new);
        report.setState(event.getNewState());

        // Assign the user based on the role
        switch (getRoleForState(event.getNewState())) {
            case OWNER -> {
                if (report.getOwner() == null) {
                    report.setOwner(user);
                }
            }
            case REVIEWER -> report.setReviewer(user);
            case VALIDATOR -> report.setValidator(user);
        }

        // Log the state transition when along with updating the report
        transitionLogRepository.save(new ReportTransitionLog(report, user, event.getNewState()));

        reportRepository.save(report);

        log.info("Report ID {} updated to state: {} by User ID {}",
                event.getReportId(), event.getNewState(), event.getUserId());
    }

    private Role getRoleForState(ReportState state) {
        return switch (state) {
            case CREATED -> Role.OWNER;
            case REVIEWED -> Role.REVIEWER;
            case VALIDATED, REFUSED -> Role.VALIDATOR; // ✅ Handles both validated and refused
            default -> throw new IllegalArgumentException("Unexpected state: " + state);
        };
    }
}

