package com.youssef.reportworkflow.controllers;

import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.User;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.dto.*;
import com.youssef.reportworkflow.dto.authentication.*;
import com.youssef.reportworkflow.utils.*;
import io.swagger.v3.oas.annotations.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Jozef
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a user with a default role and returns a success message.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Username already exists", null));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.OWNER)); // Default role

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getRoles());

        log.info("New user registered: {}", request.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("User registered successfully", new AuthResponse(token, userDTO)));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String token = jwtUtil.generateToken(user);
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getRoles());

            log.info("User logged in: {}", request.getUsername());

            return ResponseEntity.ok(new ApiResponse<>("Login successful", new AuthResponse(token, userDTO)));

        } catch (AuthenticationException ex) {
            log.warn("Failed login attempt for username: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid credentials", null));
        }
    }
}
