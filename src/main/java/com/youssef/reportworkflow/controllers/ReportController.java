package com.youssef.reportworkflow.controllers;

import com.youssef.reportworkflow.config.*;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.dto.ApiResponse;
import com.youssef.reportworkflow.service.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.http.*;
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

    /**
     * Starts a new report workflow.
     *
     * @param title The title of the report.
     * @return The created report DTO.
     */
    @PostMapping("/start")
    @Operation(summary = "Create and start a report workflow", description = "Creates a report and starts the workflow process.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Report created successfully", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized - Only owners can create reports",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ReportDTO>> createReportAndStartWorkflow(@RequestParam String title) {
        Long userId = userContext.getAuthenticatedUserId(); // Extract userId from SecurityContext
        ReportDTO reportDTO = reportService.startReportWorkflow(title, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Report created successfully", reportDTO));
    }

    /**
     * Confirms the creation of a report.
     *
     * @param reportId The ID of the report to confirm.
     * @return Success message.
     */
    @PostMapping("/{reportId}/confirm")
    @Operation(summary = "Confirm report creation", description = "Confirms report creation in the workflow process.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Report confirmed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized - Only owners can confirm reports",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> confirmReportCreation(@PathVariable Long reportId) {
        Long ownerId = userContext.getAuthenticatedUserId();
        reportService.confirmReportCreation(reportId, ownerId);

        return ResponseEntity.ok(new ApiResponse<>("Report confirmed successfully"));
    }

    /**
     * Marks a report as reviewed.
     *
     * @param reportId The ID of the report to review.
     * @return The updated report DTO.
     */
    @PostMapping("/{reportId}/review")
    @Operation(summary = "Review a report", description = "Marks a report as reviewed in the workflow process.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Report reviewed successfully", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized - Only reviewers can review reports",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ReportDTO>> reviewReport(@PathVariable Long reportId) {
        Long reviewerId = userContext.getAuthenticatedUserId();
        ReportDTO updatedReport = reportService.reviewReport(reportId, reviewerId);
        return ResponseEntity.ok(new ApiResponse<>("Report reviewed successfully", updatedReport));
    }

    /**
     * Validates or refuses a report.
     *
     * @param reportId   The ID of the report to validate or refuse.
     * @param isApproved True if the report is approved, false if refused.
     * @return The updated report DTO.
     */
    @PostMapping("/{reportId}/validate")
    @Operation(summary = "Validate or refuse a report", description = "Marks a report as validated or refused.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Report validated/refused successfully", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized - Only validators can validate/refuse reports",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Report not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ReportDTO>> validateOrRefuseReport(
            @PathVariable Long reportId,
            @RequestParam boolean isApproved) {
        Long validatorId = userContext.getAuthenticatedUserId();
        ReportDTO updatedReport = reportService.validateOrRefuseReport(reportId, validatorId, isApproved);

        String statusMessage = isApproved ? "Report validated successfully" : "Report refused successfully";

        return ResponseEntity.ok(new ApiResponse<>(statusMessage, updatedReport));
    }
}

