package com.youssef.reportworkflow.unit.controller;

import com.youssef.reportworkflow.config.*;
import com.youssef.reportworkflow.controllers.*;
import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.service.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.*;
import org.springframework.boot.test.autoconfigure.web.reactive.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.*;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Jozef
 */
@SpringBootTest
@AutoConfigureWebTestClient
class ReportControllerTest {
    //    @Autowired
//    private MockMvc mockMvc;
    @Test
    void exampleTest(@Autowired WebTestClient webClient) {
        webClient
                .get().uri("/api/v1/reports/test")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello");
    }

//    @MockitoBean
//    private ReportService reportService; // Mock service
//    @MockitoBean
//    private UserContext userContext; // Mock security context
//
//    private final Long reportId = 1L;
//    private final Long userId = 100L;
//    private final String title = "New Report";
//
//    @Test
//    void createReportAndStartWorkflow_ShouldReturn201_WithReportDTO() throws Exception {
//        // Given: Mock user ID and expected response
//        ReportDTO mockReportDTO = new ReportDTO(reportId, title, "ownerUser", ReportState.CREATED);
//
//        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
//        when(reportService.startReportWorkflow(title, userId)).thenReturn(mockReportDTO);
//
//        // When & Then: Call endpoint and verify response
//        mockMvc.perform(post("/api/v1/reports/start")
//                        .param("title", title)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.message").value("Report created successfully"))
//                .andExpect(jsonPath("$.data.id").value(reportId))
//                .andExpect(jsonPath("$.data.title").value(title))
//                .andExpect(jsonPath("$.data.state").value("CREATED"));
//
//        verify(reportService).startReportWorkflow(title, userId);
//    }
//
//    @Test
//    void confirmReportCreation_ShouldReturn200_WhenSuccessful() throws Exception {
//        // Given: Mock user ID
//        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
//        doNothing().when(reportService).confirmReportCreation(reportId, userId);
//
//        // When & Then: Call endpoint and verify response
//        mockMvc.perform(post("/api/v1/reports/{reportId}/confirm", reportId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Report confirmed successfully"));
//
//        verify(reportService).confirmReportCreation(reportId, userId);
//    }
//
//    @Test
//    void reviewReport_ShouldReturn200_WithUpdatedReport() throws Exception {
//        // Given: Mock user ID and updated report
//        ReportDTO updatedReport = new ReportDTO(reportId, title, "reviewerUser", ReportState.REVIEWED);
//
//        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
//        when(reportService.reviewReport(reportId, userId)).thenReturn(updatedReport);
//
//        // When & Then: Call endpoint and verify response
//        mockMvc.perform(post("/api/v1/reports/{reportId}/review", reportId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Report reviewed successfully"))
//                .andExpect(jsonPath("$.data.id").value(reportId))
//                .andExpect(jsonPath("$.data.state").value("REVIEWED"));
//
//        verify(reportService).reviewReport(reportId, userId);
//    }
//
//    @Test
//    void validateOrRefuseReport_ShouldReturn200_WhenValidated() throws Exception {
//        // Given: Mock user ID and validated report
//        ReportDTO validatedReport = new ReportDTO(reportId, title, "validatorUser", ReportState.VALIDATED);
//
//        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
//        when(reportService.validateOrRefuseReport(reportId, userId, true)).thenReturn(validatedReport);
//
//        // When & Then: Call endpoint with `isApproved=true`
//        mockMvc.perform(post("/api/v1/reports/{reportId}/validate", reportId)
//                        .param("isApproved", "true"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Report validated successfully"))
//                .andExpect(jsonPath("$.data.id").value(reportId))
//                .andExpect(jsonPath("$.data.state").value("VALIDATED"));
//
//        verify(reportService).validateOrRefuseReport(reportId, userId, true);
//    }
//
//    @Test
//    void validateOrRefuseReport_ShouldReturn200_WhenRefused() throws Exception {
//        // Given: Mock user ID and refused report
//        ReportDTO refusedReport = new ReportDTO(reportId, title, "validatorUser", ReportState.REFUSED);
//
//        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
//        when(reportService.validateOrRefuseReport(reportId, userId, false)).thenReturn(refusedReport);
//
//        // When & Then: Call endpoint with `isApproved=false`
//        mockMvc.perform(post("/api/v1/reports/{reportId}/validate", reportId)
//                        .param("isApproved", "false"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Report refused successfully"))
//                .andExpect(jsonPath("$.data.id").value(reportId))
//                .andExpect(jsonPath("$.data.state").value("REFUSED"));
//
//        verify(reportService).validateOrRefuseReport(reportId, userId, false);
//    }
}
