package com.youssef.reportworkflow.config;

import com.fasterxml.jackson.databind.*;
import com.youssef.reportworkflow.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.web.*;
import org.springframework.stereotype.*;

import javax.naming.*;
import java.io.*;
import java.time.*;

/**
 * @author Jozef
 */
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {
        log.warn("Unauthorized access attempt to: {}", request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(
                "You must be logged in to access this resource.",
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                request.getRequestURI(),
                Instant.now()
        );

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}
