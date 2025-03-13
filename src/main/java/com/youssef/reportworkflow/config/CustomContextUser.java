package com.youssef.reportworkflow.config;

import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.*;

import java.util.*;

/**
 * @author Jozef
 */
public class CustomContextUser extends User {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public CustomContextUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
        super(username, password, authorities);
        setId(id);
    }

    public CustomContextUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, Long id) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        setId(id);
    }
}
