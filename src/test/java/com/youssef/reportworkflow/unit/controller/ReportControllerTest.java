package com.youssef.reportworkflow.unit.controller;

import com.youssef.reportworkflow.config.*;
import com.youssef.reportworkflow.controllers.*;
import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.service.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * @author Jozef
 */
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;
    @MockitoBean private UserContext userContext;

    @Test
    void testCreateReportAndStartWorkflow_Success() throws Exception {
        Report mockReport = new Report(1L, "Sample Report", ReportState.CREATED, null, null, null, null, null, null);

        Mockito.when(userContext.getAuthenticatedUserId()).thenReturn(1L);
        Mockito.when(reportService.startReportWorkflow(anyString(), anyLong())).thenReturn(mockReport);

        mockMvc.perform(post("/api/v1/reports/start")
                        .param("title", "Sample Report")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample Report"));
    }

    @Test
    void testConfirmReportCreation_Success() throws Exception {
        Mockito.when(userContext.getAuthenticatedUserId()).thenReturn(1L);
        Mockito.doNothing().when(reportService).confirmReportCreation(anyLong(), anyLong());

        mockMvc.perform(post("/api/v1/reports/create")
                        .param("reportId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Report creation confirmed."));
    }

    @Test
    void testReviewReport_Success() throws Exception {
        Mockito.when(userContext.getAuthenticatedUserId()).thenReturn(2L);
        Mockito.doNothing().when(reportService).reviewReport(anyLong(), anyLong());

        mockMvc.perform(post("/api/v1/reports/review")
                        .param("reportId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Report reviewed successfully!"));
    }

    @Test
    void testValidateOrRefuseReport_Approved() throws Exception {
        Mockito.when(userContext.getAuthenticatedUserId()).thenReturn(3L);
        Mockito.doNothing().when(reportService).validateOrRefuseReport(anyLong(), anyLong(), eq(true));

        mockMvc.perform(post("/api/v1/reports/validate")
                        .param("reportId", "1")
                        .param("isApproved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("Validation processed!"));
    }

    @Test
    void testValidateOrRefuseReport_Refused() throws Exception {
        Mockito.when(userContext.getAuthenticatedUserId()).thenReturn(3L);
        Mockito.doNothing().when(reportService).validateOrRefuseReport(anyLong(), anyLong(), eq(false));

        mockMvc.perform(post("/api/v1/reports/validate")
                        .param("reportId", "1")
                        .param("isApproved", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("Validation processed!"));
    }
}
