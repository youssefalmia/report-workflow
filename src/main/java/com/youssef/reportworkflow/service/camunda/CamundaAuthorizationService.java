package com.youssef.reportworkflow.service.camunda;

import jakarta.annotation.*;
import lombok.*;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.authorization.*;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.*;
import org.springframework.stereotype.*;

/**
 * @author Jozef
 */
@Service
@RequiredArgsConstructor
public class CamundaAuthorizationService {

    private final AuthorizationService authorizationService;
    private final IdentityService identityService;

    private static final String COCKPIT_USERNAME = "cockpitUser"; // Read-only user
    private static final String COCKPIT_PASSWORD = "cockpitPass"; // Default password

    @PostConstruct
    public void createCockpitUser() {
        //  Step 1: Ensure the user exists in Camunda
        if (identityService.createUserQuery().userId(COCKPIT_USERNAME).singleResult() == null) {
            User user = identityService.newUser(COCKPIT_USERNAME);
            user.setFirstName("Cockpit");
            user.setLastName("User");
            user.setPassword(COCKPIT_PASSWORD);
            identityService.saveUser(user);
            System.out.println(" Camunda cockpit user created: " + COCKPIT_USERNAME);
        }

        //  Step 2: Assign Read-Only Permissions
        configureReadOnlyPermissions(COCKPIT_USERNAME);
    }

    private void configureReadOnlyPermissions(String username) {
        //  Allow viewing Camunda Cockpit UI
        grantPermission(username, Resources.APPLICATION, "cockpit", Permissions.ACCESS);

        //  Grant Read-Only access to Process Definitions, Instances, and Tasks
        grantPermission(username, Resources.PROCESS_DEFINITION, "*", Permissions.READ);
        grantPermission(username, Resources.PROCESS_INSTANCE, "*", Permissions.READ);
        grantPermission(username, Resources.TASK, "*", Permissions.READ);

        // Revoke Task Completion, Updates, and Deletion
        revokePermission(username, Resources.TASK, "*", Permissions.TASK_WORK, Permissions.UPDATE, Permissions.DELETE);
        revokePermission(username, Resources.PROCESS_INSTANCE, "*", Permissions.UPDATE, Permissions.DELETE);

        System.out.println(" Camunda cockpit user permissions set to READ-ONLY.");
    }

    private void grantPermission(String username, Resources resource, String resourceId, Permissions... permissions) {
        Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        auth.setResource(resource);
        auth.setResourceId(resourceId);
        auth.setUserId(username);
        for (Permissions permission : permissions) {
            auth.addPermission(permission);
        }
        authorizationService.saveAuthorization(auth);
    }

    private void revokePermission(String username, Resources resource, String resourceId, Permissions... permissions) {
        Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_REVOKE);
        auth.setResource(resource);
        auth.setResourceId(resourceId);
        auth.setUserId(username);
        for (Permissions permission : permissions) {
            auth.addPermission(permission);
        }
        authorizationService.saveAuthorization(auth);
    }
}
