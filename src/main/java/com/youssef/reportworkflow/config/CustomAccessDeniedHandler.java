package com.youssef.reportworkflow.config;

import com.fasterxml.jackson.databind.*;
import com.youssef.reportworkflow.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.security.web.access.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.time.*;

/**
 * @author Jozef
 */
@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("Access denied: {} - Reason: {}", request.getRequestURI(), accessDeniedException.getMessage());

        // Determine the required role based on the request path
        String requiredRole = getRequiredRoleFromRequest(request);
        String errorMessage = switch (requiredRole) {
            case "ROLE_OWNER" -> "Only Owners can start the workflow and change state to 'Created'.";
            case "ROLE_REVIEWER" -> "Only Reviewers can change the state from 'Created' to 'Reviewed'.";
            case "ROLE_VALIDATOR" -> "Only Validators can change the state from 'Reviewed' to 'Validated'.";
            default -> "Access denied: You do not have the required permissions.";
        };

        ErrorResponse errorResponse = new ErrorResponse(
                errorMessage,
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                request.getRequestURI(),
                Instant.now()
        );
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

    private String getRequiredRoleFromRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/reports/create") || path.startsWith("/api/v1/reports/start")) return "ROLE_OWNER";
        if (path.startsWith("/api/v1/reports/review")) return "ROLE_REVIEWER";
        if (path.startsWith("/api/v1/reports/validate")) return "ROLE_VALIDATOR";

        return "UNKNOWN";
    }

}
