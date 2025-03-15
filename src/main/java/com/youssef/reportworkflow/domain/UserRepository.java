package com.youssef.reportworkflow.domain;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User,Long>  {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
