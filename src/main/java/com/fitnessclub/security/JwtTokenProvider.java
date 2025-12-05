// Файл: src/main/java/com/fitnessclub/security/JwtTokenProvider.java
package com.fitnessclub.security; // Обратите внимание на пакет!

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:your-very-secure-jwt-secret-key-that-is-at-least-32-characters-long}")
    private String secret;

    @Value("${jwt.access-token-expiration:15}")
    private long accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration:7}")
    private long refreshTokenExpirationDays;

    private SecretKey getAccessSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private SecretKey getRefreshSecretKey() {
        return Keys.hmacShaKeyFor((secret + "-refresh").getBytes());
    }

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_SESSION_ID = "sessionId";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    public enum TokenType {
        ACCESS, REFRESH
    }

    public String generateAccessToken(Long userId, String username, String role, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_SESSION_ID, sessionId);
        claims.put(CLAIM_TOKEN_TYPE, TokenType.ACCESS.name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES)))
                .signWith(getAccessSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId, String username, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_SESSION_ID, sessionId);
        claims.put(CLAIM_TOKEN_TYPE, TokenType.REFRESH.name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS)))
                .signWith(getRefreshSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            getClaimsFromAccessToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            getClaimsFromRefreshToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaimsFromAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getAccessSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims getClaimsFromRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getRefreshSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserIdFromToken(String token, TokenType tokenType) {
        Claims claims = tokenType == TokenType.ACCESS ?
                getClaimsFromAccessToken(token) : getClaimsFromRefreshToken(token);
        Integer userIdInt = claims.get(CLAIM_USER_ID, Integer.class);
        return userIdInt != null ? userIdInt.longValue() : null;
    }

    public String getSessionIdFromToken(String token, TokenType tokenType) {
        Claims claims = tokenType == TokenType.ACCESS ?
                getClaimsFromAccessToken(token) : getClaimsFromRefreshToken(token);
        return claims.get(CLAIM_SESSION_ID, String.class);
    }

    public String getUsernameFromToken(String token, TokenType tokenType) {
        Claims claims = tokenType == TokenType.ACCESS ?
                getClaimsFromAccessToken(token) : getClaimsFromRefreshToken(token);
        return claims.getSubject();
    }

    public String getRoleFromAccessToken(String token) {
        Claims claims = getClaimsFromAccessToken(token);
        return claims.get(CLAIM_ROLE, String.class);
    }
}