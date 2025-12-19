package com.fitnessclub.security;

import com.fitnessclub.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final Key accessKey;
    private final Key refreshKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.access.secret}") String accessSecret,
            @Value("${jwt.refresh.secret}") String refreshSecret,
            @Value("${jwt.access.expiration-ms}") long accessTokenValidityMs,
            @Value("${jwt.refresh.expiration-ms}") long refreshTokenValidityMs
    ) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String generateAccessToken(UserDetailsImpl userDetails, Long sessionId) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenValidityMs);

        String roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("type", "ACCESS")
                .claim("userId", userDetails.getId())
                .claim("email", userDetails.getEmail())
                .claim("roles", roles)
                .claim("sessionId", sessionId)
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetailsImpl userDetails, Long sessionId) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenValidityMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("type", "REFRESH")
                .claim("userId", userDetails.getId())
                .claim("sessionId", sessionId)
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateAccessToken(String token) {
        return parseToken(token, accessKey, "ACCESS");
    }

    public Jws<Claims> validateRefreshToken(String token) {
        return parseToken(token, refreshKey, "REFRESH");
    }

    private Jws<Claims> parseToken(String token, Key key, String expectedType) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            String type = jws.getBody().get("type", String.class);
            if (!expectedType.equals(type)) {
                throw new JwtException("Invalid token type");
            }
            return jws;
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    public LocalDateTime getExpiration(Jws<Claims> jws) {
        Date exp = jws.getBody().getExpiration();
        return LocalDateTime.ofInstant(exp.toInstant(), ZoneId.systemDefault());
    }
}







