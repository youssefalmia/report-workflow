package com.youssef.reportworkflow.unit.controller;

import com.fasterxml.jackson.databind.*;
import com.youssef.reportworkflow.config.*;
import com.youssef.reportworkflow.controllers.*;
import com.youssef.reportworkflow.domain.*;
import com.youssef.reportworkflow.dto.authentication.*;
import com.youssef.reportworkflow.utils.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.*;
import org.springframework.test.context.bean.override.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Jozef
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setup() {
        Mockito.when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
    }

    @Test
    void testUserRegistration_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("testUser", "password123");

        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(any(User.class))).thenReturn(new User(1L, "testUser", "encodedPassword", Set.of()));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void testUserRegistration_Failure_UsernameExists() throws Exception {
        RegisterRequest request = new RegisterRequest("existingUser", "password123");

        Mockito.when(userRepository.findByUsername("existingUser"))
                .thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    @Test
    void testUserLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("testUser", "password123");

        User user = new User(1L, "testUser", "encodedPassword", Set.of());
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        Mockito.when(jwtUtil.generateToken(user)).thenReturn("mockedToken");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockedToken"));
    }

    @Test
    void testUserLogin_Failure_UserNotFound() throws Exception {
        LoginRequest request = new LoginRequest("nonexistentUser", "password123");

        Mockito.when(userRepository.findByUsername("nonexistentUser"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}

