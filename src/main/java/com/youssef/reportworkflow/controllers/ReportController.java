package com.youssef.reportworkflow.controllers;

import com.youssef.reportworkflow.config.*;
import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.service.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;

import java.security.*;

/**
 * @author Jozef
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report Management", description = "APIs for managing reports and workflow") // for swagger documentation
@SecurityRequirement(name = "bearerAuth") // for swagger auth header
public class ReportController {

    private final ReportService reportService;
    private final UserContext userContext;

    @PostMapping("/start")
    @Operation(summary = "Create and start a report workflow", description = "Creates a report and starts the workflow process.")
    public ResponseEntity<ApiResponse<ReportDTO>> createReportAndStartWorkflow(@RequestParam String title) {
        Long userId = userContext.getAuthenticatedUserId(); // Extract userId from SecurityContext
        ReportDTO reportDTO = reportService.startReportWorkflow(title, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Report created successfully", reportDTO));
    }

    @PostMapping("/{reportId}/confirm")
    @Operation(summary = "Confirm report creation", description = "Confirms report creation in the workflow process.")
    public ResponseEntity<ApiResponse<Void>> confirmReportCreation(@PathVariable Long reportId) {
        Long ownerId = userContext.getAuthenticatedUserId();
        reportService.confirmReportCreation(reportId, ownerId);

        return ResponseEntity.ok(new ApiResponse<>("Report confirmed successfully"));
    }

    @PostMapping("/{reportId}/review")
    @Operation(summary = "Review a report", description = "Marks a report as reviewed in the workflow process.")
    public ResponseEntity<ApiResponse<ReportDTO>> reviewReport(@PathVariable Long reportId) {
        Long reviewerId = userContext.getAuthenticatedUserId();
        ReportDTO updatedReport = reportService.reviewReport(reportId, reviewerId);

        return ResponseEntity.ok(new ApiResponse<>("Report reviewed successfully", updatedReport));
    }

    @PostMapping("/{reportId}/validate")
    @Operation(summary = "Validate or refuse a report", description = "Marks a report as validated or refused.")
    public ResponseEntity<ApiResponse<ReportDTO>> validateOrRefuseReport(
            @PathVariable Long reportId,
            @RequestParam boolean isApproved) {
        Long validatorId = userContext.getAuthenticatedUserId();
        ReportDTO updatedReport = reportService.validateOrRefuseReport(reportId, validatorId, isApproved);

        String statusMessage = isApproved ? "Report validated successfully" : "Report refused successfully";

        return ResponseEntity.ok(new ApiResponse<>(statusMessage, updatedReport));
    }
}

