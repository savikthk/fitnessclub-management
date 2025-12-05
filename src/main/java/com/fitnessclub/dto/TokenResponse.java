package com.fitnessclub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("refresh_expires_in")
    private long refreshExpiresIn;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private Long userId; // Измените на Long

    @JsonProperty("username")
    private String username;

    @JsonProperty("role")
    private String role;

    // Конструктор по умолчанию
    public TokenResponse() {
        this.tokenType = "Bearer";
    }

    // Исправленный конструктор: Long userId вместо Integer
    public TokenResponse(String accessToken, String refreshToken,
                         long expiresIn, long refreshExpiresIn,
                         String sessionId, Long userId, // Long здесь
                         String username, String role) {
        this();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.sessionId = sessionId;
        this.userId = userId; // Long
        this.username = username;
        this.role = role;
    }

    // Геттеры и сеттеры
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public long getRefreshExpiresIn() { return refreshExpiresIn; }
    public void setRefreshExpiresIn(long refreshExpiresIn) { this.refreshExpiresIn = refreshExpiresIn; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getUserId() { return userId; } // Long
    public void setUserId(Long userId) { this.userId = userId; } // Long

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}