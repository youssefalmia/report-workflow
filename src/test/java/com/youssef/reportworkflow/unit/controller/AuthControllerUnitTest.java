package com.youssef.reportworkflow.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youssef.reportworkflow.controllers.*;
import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.dto.authentication.*;
import com.youssef.reportworkflow.utils.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Jozef
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // For JSON serialization

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    /**
     * Test user registration - Success scenario.
     */
    @Test
    void register_ShouldReturn201_WhenUserIsRegisteredSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("newUser", "password123");
        User newUser = new User(1L, "newUser", "encodedPassword", Set.of(Role.OWNER));
        String mockJwtToken = "mocked-jwt-token";

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn(mockJwtToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.token").value(mockJwtToken))
                .andExpect(jsonPath("$.data.userDTO.username").value("newUser"));

        verify(userRepository).save(any(User.class));
    }

    /**
     * Test user registration - Failure when username already exists.
     */
    @Test
    void register_ShouldReturn400_WhenUsernameAlreadyExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("existingUser", "password123");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test user login - Success scenario.
     */
    @Test
    void login_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("validUser", "password123");
        User user = new User(1L, "validUser", "encodedPassword", Set.of(Role.OWNER));
        String mockJwtToken = "mocked-jwt-token";

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn(mockJwtToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value(mockJwtToken))
                .andExpect(jsonPath("$.data.userDTO.username").value("validUser"));

        verify(userRepository).findByUsername(request.getUsername());
    }

    /**
     * Test user login - Failure when user does not exist.
     */
    @Test
    void login_ShouldReturn404_WhenUserNotFound() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("nonExistentUser", "password123");

        when(userRepository.findByUsername(request.getUsername()))
                .thenReturn(Optional.empty());


        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userRepository).findByUsername(request.getUsername());
    }

    /**
     * Test user login - Failure when credentials are invalid.
     */
    @Test
    void login_ShouldReturn401_WhenCredentialsAreInvalid() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("validUser", "wrongPassword");
        User mockUser = new User(1L, "validUser", "encodedPassword", Set.of(Role.OWNER));

        when(userRepository.findByUsername(request.getUsername()))
                .thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(authenticationManager).authenticate(any());
    }
}
