package com.youssef.reportworkflow.init;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.User;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.utils.*;
import jakarta.annotation.*;
import lombok.*;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.authorization.*;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.*;
import org.springframework.boot.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * @author Jozef
 */
@Component
@RequiredArgsConstructor
public class DataInitializer {
    public static final String OWNER_USER = "ownerUser";
    public static final String REVIEWER_USER = "reviewerUser";
    public static final String VALIDATOR_USER = "validatorUser";
    public static final String MULTI_ROLE_USER = "multiRoleUser";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static final Map<String,User> userMap = new HashMap<>();

    @PostConstruct
    public void initData() {
        User owner = new User(null, OWNER_USER, passwordEncoder.encode("ownerPass"), Set.of(Role.OWNER));
        User reviewer = new User(null, REVIEWER_USER, passwordEncoder.encode("reviewerPass"), Set.of(Role.REVIEWER));
        User validator = new User(null, VALIDATOR_USER, passwordEncoder.encode("validatorPass"), Set.of(Role.VALIDATOR));
        User multiRoleUser = new User(null, MULTI_ROLE_USER, passwordEncoder.encode("multiPass"), Set.of(Role.OWNER, Role.REVIEWER, Role.VALIDATOR));

        userMap.put(OWNER_USER,owner);
        userMap.put(REVIEWER_USER,reviewer);
        userMap.put(VALIDATOR_USER,validator);
        userMap.put(MULTI_ROLE_USER,multiRoleUser);

        userRepository.saveAll(List.of(owner, reviewer, validator, multiRoleUser));
    }

    public static User getUserByUsername(String username){
        return userMap.get(username);
    }


}
