package com.youssef.reportworkflow.init;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.utils.*;
import lombok.*;
import org.springframework.boot.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * @author Jozef
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CamundaDatabaseCleaner camundaDatabaseCleaner;

    @Override
    public void run(String... args) {
//        camundaDatabaseCleaner.cleanAllData();

//        if (userRepository.count() == 0) {
//            User owner = new User(null, "ownerUser", passwordEncoder.encode("ownerPass"), Set.of(Role.OWNER));
//            User reviewer = new User(null, "reviewerUser", passwordEncoder.encode("reviewerPass"), Set.of(Role.REVIEWER));
//            User validator = new User(null, "validatorUser", passwordEncoder.encode("validatorPass"), Set.of(Role.VALIDATOR));
//            User multiRoleUser = new User(null, "string", passwordEncoder.encode("string"), Set.of(Role.OWNER, Role.REVIEWER, Role.VALIDATOR));
//
//            userRepository.saveAll(List.of(owner, reviewer, validator, multiRoleUser));
//
//            System.out.println("Pre-created users initialized!");
//        }
    }
}
