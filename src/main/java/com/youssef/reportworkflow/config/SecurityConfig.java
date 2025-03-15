package com.youssef.reportworkflow.config;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @author Jozef
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Allow all endpoints
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow necessary HTTP methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow cookies
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register",
                                "/api/v1/auth/login", "/swagger-ui/**",
                                "/v3/api-docs/**","/camunda/**",
                                "/h2-console/**").permitAll()
                        .requestMatchers("/api/v1/reports/*/confirm","/api/v1/reports/start").hasRole("OWNER") // Only Owners can create reports
                        .requestMatchers("/api/v1/reports/*/review").hasRole("REVIEWER") // Only Reviewers can review reports
                        .requestMatchers("/api/v1/reports/*/validate").hasRole("VALIDATOR") // Only Validators can validate/refuse
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler) // Catch unauthorized access to throw custom exceptions on each specific role
                ).formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

