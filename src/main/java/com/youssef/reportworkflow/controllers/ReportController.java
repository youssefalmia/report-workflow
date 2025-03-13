package com.youssef.reportworkflow.controllers;

import com.youssef.reportworkflow.config.*;
import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.service.*;
import io.swagger.v3.oas.annotations.security.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;

import java.security.*;

/**
 * @author Jozef
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // for swagger auth header
public class ReportController {

    private final ReportService reportService;
    private final UserContext userContext;

    @PostMapping("/start")
    public ResponseEntity<Report> createReportAndStartWorkflow(@RequestParam String title) {
        Long userId = userContext.getAuthenticatedUserId(); // Extract userId from SecurityContext
        return ResponseEntity.ok(reportService.startReportWorkflow(title, userId));
    }

    @PostMapping("/create")
    public ResponseEntity<String> confirmReportCreation(@RequestParam Long reportId) {
        Long ownerId = userContext.getAuthenticatedUserId();

        reportService.confirmReportCreation(reportId, ownerId);
        return ResponseEntity.ok("Report creation confirmed.");
    }

    @PostMapping("/review")
    public ResponseEntity<String> reviewReport(@RequestParam Long reportId) {
        Long userId = userContext.getAuthenticatedUserId();
        reportService.reviewReport(reportId, userId);
        return ResponseEntity.ok("Report reviewed successfully!");
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateOrRefuseReport(@RequestParam Long reportId,
                                                         @RequestParam boolean isApproved) {
        Long userId = userContext.getAuthenticatedUserId();
        reportService.validateOrRefuseReport(reportId, userId, isApproved);
        return ResponseEntity.ok("Validation processed!");
    }
}

