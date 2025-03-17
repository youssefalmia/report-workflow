package com.youssef.reportworkflow.unit.service;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.exception.*;
import com.youssef.reportworkflow.mapper.*;
import com.youssef.reportworkflow.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.test.util.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Jozef
 */
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportWorkflowService workflowService;
    @Mock
    private ReportMapper reportMapper;
    @InjectMocks
    private ReportService reportService;

    private final Long ownerId = 1L;
    private final Long reviewerId = 2L;
    private final Long validatorId = 2L;
    private final String title = "New Report";
    private final Long reportId = 1L;

    @Test
    void startReportWorkflow_shouldCreateReportAndStartWorkflow() {
        // Given: A mock user
        User mockOwner = new User(ownerId, "ownerUser", "hashedPassword", Set.of(Role.OWNER));

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockOwner));

        // Mock save behavior to simulate JPA assigning an ID
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report reportToSave = invocation.getArgument(0);
            reportToSave.setId(10L); // Simulating database ID assignment
            return reportToSave;
        });

        // Workflow service should return a String ( process id )
        when(workflowService.startWorkflow(anyLong(), anyLong())).thenReturn("mock-process-instance-id");

        // All the above tested the internal pieces of my method, to provide the output/behaviour of each piece
        // we now can call it

        // When calling the method
        ReportDTO result = reportService.startReportWorkflow(title, ownerId);

        // Then verify repository interactions
        verify(userRepository).findById(ownerId); // Ensures the user exists
        verify(reportRepository).save(any(Report.class)); // Ensures the report is saved

        // Ensures workflow is started AFTER saving the report
        verify(workflowService).startWorkflow(eq(10L), eq(ownerId)); // to ensure that it is called aftersaving ( 10L )

        // Validate DTO response
        assertNotNull(result);
        assertEquals(10L, result.getId()); // Ensures ID is properly assigned
        assertEquals(title, result.getTitle());
        assertEquals(ReportState.CREATED, result.getState());
    }

    @Test
    void startReportWorkflow_shouldThrowExceptionWhenUserNotFound() {
        // Given user does not exist
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        // Expect Exception
        assertThrows(UserNotFoundException.class, () -> reportService.startReportWorkflow(title, ownerId));

        // Then ensure no report is saved, and no workflow is started
        verify(reportRepository, never()).save(any(Report.class));
        verify(workflowService, never()).startWorkflow(anyLong(), anyLong());
    }

    @Test
    void confirmReportCreation_shouldCallWorkflowService() {
        // when calling the method
        reportService.confirmReportCreation(reportId, ownerId);

        // then verify that workflowService.createReport was called once
        verify(workflowService, times(1)).createReport(eq(reportId), eq(ownerId));

        verifyNoMoreInteractions(workflowService);
    }

    @Test
    void validateOrRefuseReport_shouldReviewSuccessfully() {
        // given
        User mockuser = new User(reviewerId, "reviewerUsername", "reviewerPass", Set.of(Role.REVIEWER));
        Report report = new Report(reportId, title, ReportState.CREATED, null, mockuser, null, LocalDateTime.now(), new ArrayList<>());

        ReportDTO expectedDTO = new ReportDTO(reportId, title, "reviewerUsername", ReportState.REVIEWED);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(mockuser));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.CREATED);
        when(reportMapper.toDTO(report)).thenReturn(expectedDTO);

        // when calling the method
        ReportDTO result = reportService.reviewReport(reportId, reviewerId);

        // then
        // ensure repos and services were called
        verify(reportRepository).findById(reportId);
        verify(userRepository).findById(reviewerId);
        verify(workflowService).reviewReport(reportId, reviewerId);
        verify(reportMapper).toDTO(report);

        assertNotNull(result);
        assertEquals(expectedDTO, result);
    }

    @Test
    void reviewReport_ShouldThrowIfReportNotFound() {
        // Given No report found
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Expect exception
        assertThrows(ReportNotFoundException.class, () -> reportService.reviewReport(reportId, reviewerId));

        // Ensure nothing else is called
        verifyNoInteractions(userRepository, workflowService, reportMapper);
    }

    @Test
    void reviewReport_ShouldThrowIfReviewerNotFound() {
        // Given: A report exists but reviewer is missing
        Report report = new Report(reportId, title, ReportState.CREATED, null, null, null, LocalDateTime.now(), new ArrayList<>());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.empty());
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.CREATED);
        // Expect exception
        assertThrows(UserNotFoundException.class, () -> reportService.reviewReport(reportId, reviewerId));
        // verify that it's never called ( since the error should be thrown
        verify(workflowService, never()).reviewReport(anyLong(), anyLong());

        // Ensure workflow service is never called
        verifyNoInteractions(reportMapper);
    }

    @Test
    void reviewReport_ShouldThrowIfUserNotAReviewer() {
        // Given: A user without the `REVIEWER` role
        User nonReviewer = new User(reviewerId, "someUser", "hashedPassword", Set.of(Role.OWNER)); // ❌ Not a reviewer
        Report report = new Report(reportId, title, ReportState.CREATED, null, null, null, LocalDateTime.now(), new ArrayList<>());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(nonReviewer));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.CREATED);
        // Expect exception
        assertThrows(ReviewerPermissionException.class, () -> reportService.reviewReport(reportId, reviewerId));

        verify(workflowService, never()).reviewReport(anyLong(), anyLong());
        // Ensure workflow service is never called
        verifyNoInteractions(reportMapper);
    }

    @Test
    void validateOrRefuseReport_ShouldValidateSuccessfully() {
        // Given: A validator user and a reviewed report
        User validator = new User(validatorId, "validatorUser", "hashedPassword", Set.of(Role.VALIDATOR));
        Report report = new Report(reportId, title, ReportState.REVIEWED, null, null, null, LocalDateTime.now(), new ArrayList<>());

        ReportDTO expectedDTO = new ReportDTO(reportId, title, "validatorUser", ReportState.VALIDATED);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(userRepository.findById(validatorId)).thenReturn(Optional.of(validator));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.REVIEWED);
        when(reportMapper.toDTO(report)).thenReturn(expectedDTO);

        // When: Calling the method
        ReportDTO result = reportService.validateOrRefuseReport(reportId, validatorId, true);

        // Then: Ensure interactions are correct
        verify(reportRepository).findById(reportId);
        verify(userRepository).findById(validatorId);
        verify(workflowService).processValidationDecision(reportId, validatorId, true);
        verify(reportMapper).toDTO(report);

        // Assert DTO correctness
        assertNotNull(result);
        assertEquals(expectedDTO, result);
    }

    @Test
    void validateOrRefuseReport_ShouldRefuseSuccessfully() {
        // Given: Same setup as validation but different expected state
        User validator = new User(validatorId, "validatorUser", "hashedPassword", Set.of(Role.VALIDATOR));
        Report report = new Report(reportId, title, ReportState.REVIEWED, null, null, null, LocalDateTime.now(), new ArrayList<>());

        ReportDTO expectedDTO = new ReportDTO(reportId, title, "validatorUser", ReportState.REFUSED);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(userRepository.findById(validatorId)).thenReturn(Optional.of(validator));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.REVIEWED);
        when(reportMapper.toDTO(report)).thenReturn(expectedDTO);

        // When
        ReportDTO result = reportService.validateOrRefuseReport(reportId, validatorId, false);

        // Then
        verify(reportRepository).findById(reportId);
        verify(userRepository).findById(validatorId);
        verify(workflowService).processValidationDecision(reportId, validatorId, false);
        verify(reportMapper).toDTO(report);

        // Assert DTO correctness
        assertNotNull(result);
        assertEquals(expectedDTO, result);
    }

    @Test
    void validateOrRefuseReport_ShouldThrowIfReportNotFound() {
        // Given: No report exists
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Expect Exception
        assertThrows(ReportNotFoundException.class, () -> reportService.validateOrRefuseReport(reportId, validatorId, true));

        // Ensure nothing else runs
        verifyNoInteractions(userRepository, workflowService, reportMapper);
    }

    @Test
    void validateOrRefuseReport_ShouldThrowIfStateNotReviewed() {
        // Given: Report exists but in the wrong state
        Report report = new Report(reportId, title, ReportState.CREATED, null, null, null, LocalDateTime.now(), new ArrayList<>());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.CREATED);

        // Expect Exception
        assertThrows(InvalidReportStateException.class, () -> reportService.validateOrRefuseReport(reportId, validatorId, true));
        verify(workflowService, never()).processValidationDecision(anyLong(), anyLong(), anyBoolean());

        verifyNoInteractions(userRepository, reportMapper);
    }

    @Test
    void validateOrRefuseReport_ShouldThrowIfValidatorNotFound() {
        // Given: Report exists but validator doesn't
        Report report = new Report(reportId, title, ReportState.REVIEWED, null, null, null, LocalDateTime.now(), new ArrayList<>());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.REVIEWED);
        when(userRepository.findById(validatorId)).thenReturn(Optional.empty());

        // Expect Exception
        assertThrows(UserNotFoundException.class, () -> reportService.validateOrRefuseReport(reportId, validatorId, true));

        verify(workflowService, never()).processValidationDecision(anyLong(), anyLong(), anyBoolean());

        verifyNoInteractions(reportMapper);
    }

    @Test
    void validateOrRefuseReport_ShouldThrowIfUserNotAValidator() {
        // Given: A user without the `VALIDATOR` role
        User nonValidator = new User(validatorId, "someUser", "hashedPassword", Set.of(Role.OWNER)); // ❌ Not a validator
        Report report = new Report(reportId, title, ReportState.REVIEWED, null, null, null, LocalDateTime.now(), new ArrayList<>());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(workflowService.getReportState(reportId)).thenReturn(ReportState.REVIEWED);
        when(userRepository.findById(validatorId)).thenReturn(Optional.of(nonValidator));

        // Expect Exception
        assertThrows(ValidatorPermissionException.class, () -> reportService.validateOrRefuseReport(reportId, validatorId, true));

        verify(workflowService, never()).processValidationDecision(anyLong(), anyLong(), anyBoolean());

        verifyNoInteractions(reportMapper);
    }
}
