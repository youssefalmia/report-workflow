package com.youssef.reportworkflow.utils;

import com.youssef.reportworkflow.domain.User;
import com.youssef.reportworkflow.domain.enums.*;
import com.youssef.reportworkflow.exception.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.*;
import io.jsonwebtoken.security.*;
import jakarta.annotation.*;
import lombok.extern.slf4j.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;

import javax.crypto.*;
import java.util.*;
import java.util.function.*;

/**
 * @author Jozef
 */
@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}") // Load from application.properties
    private String secretKey;

    @Value("${jwt.expiration.access-token}") // Load from application.properties
    private long accessTokenExpiration;
    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        // Decode secret key once and store it
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)) // 10 hours validity
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public String extractUserId(String token) {
        Object userId = extractClaim(token, claims -> claims.get("userId"));
        return userId != null ? String.valueOf(userId) : null;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if a token is expired.
     */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Generic method to extract a claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts claims from a token.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token expired: {}", e.getMessage());
            throw new TokenExpirationException();
        } catch (JwtException e) {
            log.warn("JWT Token is invalid: {}", e.getMessage());
            throw new TokenValidationException();
        }
    }
}
