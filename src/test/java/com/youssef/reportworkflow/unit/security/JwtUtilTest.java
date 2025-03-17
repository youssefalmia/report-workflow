package com.youssef.reportworkflow.unit.security;

import com.youssef.reportworkflow.domain.User;
import com.youssef.reportworkflow.exception.*;
import com.youssef.reportworkflow.utils.JwtUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.*;
import io.jsonwebtoken.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jozef
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:report_db_test;DB_CLOSE_DELAY=-1"
})
class JwtUtilTest {
    @MockitoSpyBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() throws Exception {
        // Use reflection to set the private `secretKey`
        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtUtil, "sehudjioskeflsfkmlsefesfgsdfklnsefsfsefsejksef");

        // Manually call init() to initialize signingKey
        jwtUtil.init();
    }

    @Test
    void testGenerateToken_Success() {
        User user = new User(1L, "testUser", "testPassword", Set.of());

        String token = jwtUtil.generateToken(user);
        assertThat(token).isNotEmpty();
    }

    @Test
    void testExtractUsername_Success() {
        User user = new User(1L, "testUser", "testPassword", Set.of());

        String token = jwtUtil.generateToken(user);
        String extractedUsername = jwtUtil.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("testUser");
    }

    @Test
    void testExtractUserId_Success() {
        User user = new User(1L, "testUser", "testPassword", Set.of());

        String token = jwtUtil.generateToken(user);
        String extractedUserId = jwtUtil.extractUserId(token);

        assertThat(extractedUserId).isEqualTo("1");
    }

    @Test
    void testValidateToken_Success() {
        User user = new User(1L, "testUser", "testPassword", Set.of());

        String token = jwtUtil.generateToken(user);
        UserDetails mockUserDetails = Mockito.mock(UserDetails.class);
        Mockito.when(mockUserDetails.getUsername()).thenReturn("testUser");

        boolean isValid = jwtUtil.validateToken(token, mockUserDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    void testValidateToken_Expired() {
        User user = new User(1L, "testUser", "testPassword", Set.of());

        // Manually create an expired token
        String expiredToken = Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(new Date(System.currentTimeMillis() - 100000)) // Expired
                .expiration(new Date(System.currentTimeMillis() - 50000)) // Already past
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode("sehudjioskeflsfkmlsefesfgsdfklnsefsfsefsejksef")))
                .compact();

        assertThrows(TokenExpirationException.class, () -> jwtUtil.isTokenExpired(expiredToken));
    }

    @Test
    void testValidateToken_Invalid() {
        // Create an intentionally malformed JWT token
        String invalidToken = "this.is.not.a.valid.jwt";

        assertThrows(TokenValidationException.class, () -> jwtUtil.extractUsername(invalidToken));
    }
}
