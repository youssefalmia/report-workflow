package com.youssef.reportworkflow.unit.repository;

import com.youssef.reportworkflow.*;
import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.init.*;
import jakarta.inject.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.security.test.context.support.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import java.util.*;

/**
 * @author Jozef
 */
@SpringBootTest
class UserRepositoryTest {
    @Inject
    UserRepository userRepository;

    @BeforeEach
    void setup(@Autowired TestDataInitializer testDataInitializer) {
        testDataInitializer.initTestData(); // Explicitly initialize test data
    }

    @Test
    @DisplayName("Should find User by username")
    void shouldFindUserByUsername() {
        Optional<User> result = userRepository.findByUsername(TestDataInitializer.OWNER_USER);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(TestDataInitializer.OWNER_USER);
    }

    @Test
    @DisplayName("Should not find User for a non-existing username")
    void shouldNotFindNonExistingUserByUsername() {
        Optional<User> result = userRepository.findByUsername("non_existing_user");

        assertThat(result).isEmpty();
    }
}
