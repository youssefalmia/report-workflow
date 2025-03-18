package com.youssef.reportworkflow.integration;

import com.youssef.reportworkflow.config.*;
import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.domain.enums.Role;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.exception.*;
import com.youssef.reportworkflow.service.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Jozef
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoSpyBean
    private ReportService reportService;
    @MockitoBean
    private ReportRepository reportRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private ReportWorkflowService workflowService;
    @MockitoBean
    private UserContext userContext;
    private final Long reportId = 1L;
    private final Long userId = 100L;
    private final String title = "New Report";

    @Test
    @WithMockUser(username = "testUser", roles = "OWNER")
    void createReportAndStartWorkflow_ShouldReturn201_WithReportDTO() throws Exception {
        // Given: Mock user ID and expected response
        User mockOwner = new User(userId, "ownerUser", "password", Set.of(Role.OWNER));
        Report mockReport = new Report(reportId, title, ReportState.CREATED, mockOwner, null, null, LocalDateTime.now(), new ArrayList<>());
        ReportDTO mockReportDTO = new ReportDTO(reportId, title, "ownerUser", ReportState.CREATED);

        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockOwner));
        when(reportRepository.save(any(Report.class))).thenReturn(mockReport);
        when(reportService.startReportWorkflow(title, userId)).thenReturn(mockReportDTO);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/reports/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"" + title + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Report created successfully"))
                .andExpect(jsonPath("$.data.id").value(reportId))
                .andExpect(jsonPath("$.data.title").value(title))
                .andExpect(jsonPath("$.data.state").value("CREATED"));

        verify(reportService).startReportWorkflow(title, userId);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "OWNER")
    void confirmReportCreation_ShouldReturn200_WhenSuccessful() throws Exception {
        // Given: Mock user ID
        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
        doNothing().when(reportService).confirmReportCreation(reportId, userId);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/reports/{reportId}/confirm", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Report confirmed successfully"));

        verify(reportService).confirmReportCreation(reportId, userId);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "REVIEWER")
    void reviewReport_ShouldReturn200_WithUpdatedReport() throws Exception {
        // Given: Mock user ID and updated report
        User mockOwner = new User(userId, "ownerUser", "password", Set.of(Role.OWNER));
        Report mockReport = new Report(reportId, title, ReportState.CREATED, mockOwner, null, null, LocalDateTime.now(),  new ArrayList<>());
        ReportDTO updatedReport = new ReportDTO(reportId, title, "reviewerUser", ReportState.REVIEWED);
        User mockReviewer = new User(userId, "validatorUser", "password", Set.of(Role.REVIEWER));

        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.CREATED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockReviewer));
        when(reportService.reviewReport(reportId, userId)).thenReturn(updatedReport);

        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/reports/{reportId}/review", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Report reviewed successfully"))
                .andExpect(jsonPath("$.data.id").value(reportId))
                .andExpect(jsonPath("$.data.state").value("REVIEWED"));

        verify(reportService).reviewReport(reportId, userId);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "VALIDATOR")
    void validateOrRefuseReport_ShouldReturn200_WhenValidated() throws Exception {
        // Given: Mock user ID and validated report
        ReportDTO validatedReport = new ReportDTO(reportId, title, "validatorUser", ReportState.VALIDATED);
        User mockOwner = new User(userId, "ownerUser", "password", Set.of(Role.OWNER));
        Report mockReport = new Report(reportId, title, ReportState.REVIEWED, mockOwner, null, null, LocalDateTime.now(),  new ArrayList<>());
        User mockValidator = new User(userId, "validatorUser", "password", Set.of(Role.VALIDATOR));

        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.REVIEWED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockValidator));
        when(reportService.validateOrRefuseReport(reportId, userId, true)).thenReturn(validatedReport);

        // When & Then: Call endpoint with `isApproved=true`
        mockMvc.perform(post("/api/v1/reports/{reportId}/validate", reportId)
                        .param("isApproved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Report validated successfully"))
                .andExpect(jsonPath("$.data.id").value(reportId))
                .andExpect(jsonPath("$.data.state").value("VALIDATED"));

        verify(reportService).validateOrRefuseReport(reportId, userId, true);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "VALIDATOR")
    void validateOrRefuseReport_ShouldReturn200_WhenRefused() throws Exception {
        // Given: Mock user ID and refused report
        Report report = new Report(reportId, title, ReportState.REVIEWED, new User(userId, "validatorUser", "password", Set.of(Role.VALIDATOR)), null, null, LocalDateTime.now(), new ArrayList<>());
        ReportDTO refusedReport = new ReportDTO(reportId, title, "validatorUser", ReportState.REFUSED);
        User mockValidator = new User(userId, "validatorUser", "password", Set.of(Role.VALIDATOR));

        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report)); // Ensure report exists
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.REVIEWED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockValidator));
        when(reportService.validateOrRefuseReport(reportId, userId, false)).thenReturn(refusedReport);

        // When & Then: Call endpoint with `isApproved=false`
        mockMvc.perform(post("/api/v1/reports/{reportId}/validate", reportId)
                        .param("isApproved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Report refused successfully"))
                .andExpect(jsonPath("$.data.id").value(reportId))
                .andExpect(jsonPath("$.data.state").value("REFUSED"));

        verify(reportService).validateOrRefuseReport(reportId, userId, false);
    }

    @Test
    @WithAnonymousUser
    void createReportAndStartWorkflow_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        // When & Then: Call the endpoint without authentication
        mockMvc.perform(post("/api/v1/reports/start")
                        .param("title", title)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "REVIEWER") // Not an OWNER
    void createReportAndStartWorkflow_ShouldReturn403_WhenUserNotOwner() throws Exception {
        mockMvc.perform(post("/api/v1/reports/start")
                        .param("title", title)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "REVIEWER")
    void reviewReport_ShouldReturn404_WhenReportNotFound() throws Exception {
        // Given: The service throws ReportNotFoundException when report is not found
        doCallRealMethod().when(reportService).reviewReport(anyLong(), anyLong());


        // When & Then: Call endpoint and verify response
        mockMvc.perform(post("/api/v1/reports/{reportId}/review", reportId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Report with ID " + reportId + " not found"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "REVIEWER")
    void reviewReport_ShouldReturn403_WhenInvalidStateTransition() throws Exception {
        User mockReviwer = new User(userId, "reviewerUser", "password", Set.of(Role.REVIEWER));
        User mockOwner = new User(userId, "ownerUser", "password", Set.of(Role.OWNER));
        Report mockReport = new Report(reportId, title, ReportState.CREATED,  mockOwner, mockReviwer,null, LocalDateTime.now(),  new ArrayList<>());

        when(userContext.getAuthenticatedUserId()).thenReturn(userId);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockReviwer));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.CREATED);
        // Service throws an InvalidReportStateException
        when(reportService.reviewReport(reportId, userId))
                .thenThrow(new InvalidReportStateException(ReportState.CREATED, ReportState.REVIEWED));

        //  Expect a 403 Forbidden error response
        mockMvc.perform(post("/api/v1/reports/{reportId}/review", reportId))
                .andExpect(status().isForbidden()) // Because your exception handler maps it to 403
                .andExpect(jsonPath("$.message").value("Invalid state transition. Expected: CREATED, but was: REVIEWED"))
                .andExpect(jsonPath("$.timestamp").exists()) // If included in ErrorResponse
                .andExpect(jsonPath("$.path").value("/api/v1/reports/" + reportId + "/review")); // Validate error path
    }

    @Test
    @WithMockUser(username = "testUser", roles = "OWNER")
    void createReportAndStartWorkflow_ShouldReturn400_WhenTitleIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/reports/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }


}
