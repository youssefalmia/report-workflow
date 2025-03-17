package com.youssef.reportworkflow.service;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.exception.*;
import com.youssef.reportworkflow.mapper.*;
import lombok.*;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;

import java.time.*;

/**
 * @author Jozef
 */
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportWorkflowService workflowService;
    private final ReportMapper reportMapper;

    /**
     * Starts a new report workflow.
     *
     * @param title   The title of the report.
     * @param ownerId The ID of the report owner.
     * @return The created report as a DTO.
     * @throws UserNotFoundException if the owner does not exist.
     */
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public ReportDTO startReportWorkflow(String title, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(UserNotFoundException::new);

        Report report = new Report();
        report.setTitle(title);
        report.setOwner(owner);

        Report savedReport = reportRepository.save(report);

        // Start workflow, workflowService manages the state using BPM
        workflowService.startWorkflow(savedReport.getId(), ownerId);

        return new ReportDTO(report.getId(), report.getTitle(), owner.getUsername(), ReportState.CREATED);
    }

    /**
     * Confirms the creation of a report in the workflow.
     *
     * @param reportId The ID of the report to confirm.
     * @param ownerId  The ID of the owner confirming the report.
     */
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public void confirmReportCreation(Long reportId, Long ownerId) {
        workflowService.createReport(reportId, ownerId);
    }

    /**
     * Marks a report as reviewed.
     *
     * @param reportId   The ID of the report to review.
     * @param reviewerId The ID of the reviewer.
     * @return The updated report as a DTO.
     * @throws ReportNotFoundException if the report does not exist.
     * @throws UserNotFoundException if the reviewer does not exist.
     * @throws ReviewerPermissionException if the user does not have the REVIEWER role.
     * @throws InvalidReportStateException if the report is not in the expected state.
     */
    @Transactional
    @PreAuthorize("hasRole('REVIEWER')")
    public ReportDTO reviewReport(Long reportId, Long reviewerId) {
        Report report = getReportById(reportId);

        // Ensure the state is valid using BPMN instead of database
        validateReportState(reportId, ReportState.CREATED);

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(UserNotFoundException::new);

        if (!reviewer.getRoles().contains(Role.REVIEWER)) {
            throw new ReviewerPermissionException();
        }

        // Call workflow service to transition the state
        workflowService.reviewReport(reportId, reviewerId);


        ReportDTO reportDTO = reportMapper.toDTO(report);
        reportDTO.setState(ReportState.REVIEWED);

        return reportDTO;
    }

    /**
     * Validates or refuses a report based on approval status.
     *
     * @param reportId    The ID of the report to validate or refuse.
     * @param validatorId The ID of the validator.
     * @param isApproved  True if the report is approved, false if refused.
     * @return The updated report as a DTO.
     * @throws ReportNotFoundException if the report does not exist.
     * @throws UserNotFoundException if the validator does not exist.
     * @throws ValidatorPermissionException if the user does not have the VALIDATOR role.
     * @throws InvalidReportStateException if the report is not in the expected state.
     */
    @Transactional
    @PreAuthorize("hasRole('VALIDATOR')")
    public ReportDTO validateOrRefuseReport(Long reportId, Long validatorId, boolean isApproved) {
        Report report = getReportById(reportId);

        // Ensure the state is valid using BPMN instead of database
        validateReportState(reportId, ReportState.REVIEWED);

        User validator = userRepository.findById(validatorId)
                .orElseThrow(UserNotFoundException::new);

        if (!validator.getRoles().contains(Role.VALIDATOR)) {
            throw new ValidatorPermissionException();
        }

        // Delegate decision processing to workflow service
        workflowService.processValidationDecision(reportId, validatorId, isApproved);

        ReportDTO reportDTO = reportMapper.toDTO(report);
        reportDTO.setState(isApproved ? ReportState.VALIDATED : ReportState.REFUSED);

        return reportDTO;
    }

    /**
     * Fetches the report and updates its state from the workflow engine.
     *
     * @param reportId The ID of the report.
     * @return The updated report entity.
     * @throws ReportNotFoundException if the report does not exist.
     */
    private Report getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
        // Fetch latest state from BPMN
        ReportState workflowState = workflowService.getReportState(reportId);

        if (workflowState != null) {
            report.setState(workflowState);
        }

        return report;
    }

    /**
     * Ensures report state is valid before executing a workflow action.
     *
     * @param reportId      The ID of the report.
     * @param expectedState The expected report state before transition.
     * @throws InvalidReportStateException if the report is not in the expected state.
     */
    private void validateReportState(Long reportId, ReportState expectedState) {
        ReportState currentState = workflowService.getReportState(reportId);
        if (currentState != expectedState) {
            throw new InvalidReportStateException(expectedState, currentState);
        }
    }
}
/*
 DESIGN NOTE: TEMPORARY FAKE STATE UPDATE IN API RESPONSE for `reviewReport()` and `validateOrRefuseReport()` methods

&& Context:
- The actual report state update is **event-driven** and happens asynchronously via `ReportStateEventListener`.
- However, because the event runs in a separate thread, **the returned report might still show the old state** when this method completes.
- To prevent confusion, we **manually update the returned DTO** to show the expected state immediately.

&& Why This Works for Our Use Case:
- Reviewing a report is not an instant-impact action** like a transaction or booking.
- Multiple actors are involved (Owner, Reviewer, Validator)**, and they interact over time.
- If the user fetches the report later, they will get the correct state from the database.**

&& Why Not Make It Fully Synchronous?
1- Blocking Approach (Not Used)**
   - We could force `workflowService.reviewReport()` to wait until the event completes.
   - This would slow down the API and defeat the purpose of an **event-driven system**.

2- Async Without Faking the Update (Not Used for Now)
   - The API would return the old state, which might confuse users.
   - Users expect to see "REVIEWED" right after calling this API.

3- Future Improvements (Hybrid approach)
    - Don't fake the data, use a SSE to notify clients when the update completes.

I personally prefer the SSE approach, but I think we have to know our exact needs, since we don't even have a UI client
yet, but if I were to choose, I would go with the SSE approach. and make and endpoint to which the client suscribe and return the
updated report as soon as the event is processed.
I personally prefer the SSE approach, but before implementing it, we should define our exact needs.
Since we don’t have a UI client yet, and this isn't a real world project, it might not be necessary right now.
However, if I had to implement it, I think the best way to handle this would be to provide
an endpoint where clients can subscribe and receive the updated report as soon as the event is processed.


Todo: Discuss this with Mohamed or implement the hybrid approach later.
*/
