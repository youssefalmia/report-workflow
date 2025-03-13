package com.youssef.reportworkflow.config;

import com.youssef.reportworkflow.exception.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;

/**
 * @author Jozef
 */
@Component
public class UserContext {
    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomContextUser)) {
            throw new UnauthorizedUserException();
        }

        return ((CustomContextUser) authentication.getPrincipal()).getId();
    }
}

