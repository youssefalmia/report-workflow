package com.youssef.reportworkflow;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.domain.enums.Role;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * @author Jozef
 */
@Component
@RequiredArgsConstructor
public class TestDataInitializer {

    public static final String OWNER_USER = "ownerUser";
    public static final String REVIEWER_USER = "reviewerUser";
    public static final String VALIDATOR_USER = "validatorUser";
    public static final String MULTI_ROLE_USER = "multiRoleUser";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void initTestData() {
        userRepository.deleteAll(); // Ensure a clean test database

        User owner = new User(null, OWNER_USER, passwordEncoder.encode("ownerPass"), Set.of(Role.OWNER));
        User reviewer = new User(null, REVIEWER_USER, passwordEncoder.encode("reviewerPass"), Set.of(Role.REVIEWER));
        User validator = new User(null, VALIDATOR_USER, passwordEncoder.encode("validatorPass"), Set.of(Role.VALIDATOR));
        User multiRoleUser = new User(null, MULTI_ROLE_USER, passwordEncoder.encode("multiPass"), Set.of(Role.OWNER, Role.REVIEWER, Role.VALIDATOR));

        userRepository.saveAll(List.of(owner, reviewer, validator, multiRoleUser));
    }
}

