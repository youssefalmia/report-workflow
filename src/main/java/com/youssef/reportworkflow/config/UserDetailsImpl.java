package com.youssef.reportworkflow.config;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.User;
import com.youssef.reportworkflow.domain.enums.*;
import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * @author Jozef
 */
@Service
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        User user = optionalUser.get();

        Set<Role> roles = user.getRoles();

        List<GrantedAuthority> authorities = new ArrayList<>();

        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name())));

        String finalPassword = (user.getPassword() != null && !user.getPassword().isEmpty())
                ? user.getPassword()
                : "{noop}guest";  // If no password is provided

        return new CustomContextUser(
                user.getUsername(), finalPassword, authorities, user.getId());
    }
}

