package com.youssef.reportworkflow;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.init.*;
import lombok.extern.slf4j.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;

import java.util.*;

/**
 * @author Jozef
 */
@Slf4j
public class TestUtils {
    public static void authenticateUser(String username) {
        User user = DataInitializer.userMap.get(username);

        List<GrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name())));

        Authentication authentication = new TestingAuthenticationToken(username, user.getPassword(), authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Authenticated User {} {}", username, authentication);
    }
}
