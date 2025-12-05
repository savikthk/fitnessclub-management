package com.fitnessclub.controller;

import com.fitnessclub.dto.LoginRequest;
import com.fitnessclub.dto.MessageResponse;
import com.fitnessclub.dto.SignupRequest;
import com.fitnessclub.dto.TokenResponse;
import com.fitnessclub.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            authService.registerUser(signupRequest);
            return ResponseEntity.ok(new MessageResponse("Пользователь успешно зарегистрирован!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Аутентификация с JWT токенами (заменяет signin)
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request) {
        try {
            AuthService.TokenPair tokenPair = authService.authenticateUser(
                    loginRequest.getUsername(),
                    loginRequest.getPassword(),
                    request
            );

            // Получаем ID пользователя из токена
            Long userId = authService.getUserIdFromAccessToken(tokenPair.getAccessToken());

            // TODO: Получить реальную роль пользователя из БД
            String role = "MEMBER"; // Временное значение

            TokenResponse tokenResponse = new TokenResponse(
                    tokenPair.getAccessToken(),
                    tokenPair.getRefreshToken(),
                    tokenPair.getAccessTokenExpiresIn(),
                    tokenPair.getRefreshTokenExpiresIn(),
                    tokenPair.getSessionId(),
                    userId,
                    loginRequest.getUsername(),
                    role
            );

            return ResponseEntity.ok(tokenResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Старый эндпоинт для обратной совместимости
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signinOld(@Valid @RequestBody LoginRequest loginRequest,
                                       HttpServletRequest request) {
        // Перенаправляем на новый endpoint
        return authenticateUser(loginRequest, request);
    }

    /**
     * Обновление токенов по refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam("refresh_token") String refreshToken,
                                          HttpServletRequest request) {
        try {
            AuthService.TokenPair tokenPair = authService.refreshTokens(refreshToken, request);

            Long userId = authService.getUserIdFromAccessToken(tokenPair.getAccessToken());

            TokenResponse tokenResponse = new TokenResponse(
                    tokenPair.getAccessToken(),
                    tokenPair.getRefreshToken(),
                    tokenPair.getAccessTokenExpiresIn(),
                    tokenPair.getRefreshTokenExpiresIn(),
                    tokenPair.getSessionId(),
                    userId,
                    null, // username можно получить из токена если нужно
                    null  // role можно получить из токена если нужно
            );

            return ResponseEntity.ok(tokenResponse);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Выход из системы
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestParam("refresh_token") String refreshToken) {
        try {
            authService.logout(refreshToken);
            return ResponseEntity.ok(new MessageResponse("Успешный выход из системы"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Выход из всех устройств
     */
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Invalid Authorization header"));
            }

            String accessToken = authHeader.substring(7);
            Long userId = authService.getUserIdFromAccessToken(accessToken);

            authService.logoutAll(userId);
            return ResponseEntity.ok(new MessageResponse("Успешный выход из всех устройств"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Проверка валидности токена
     */
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        boolean isValid = authService.validateAccessToken(token);

        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Token is valid"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Token is invalid or expired"));
        }
    }

    /**
     * Эндпоинт для проверки пароля
     */
    @PostMapping("/validate-password")
    public ResponseEntity<?> validatePassword(@RequestBody String password) {
        boolean isValid = authService.validatePassword(password);
        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Пароль надежный"));
        } else {
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Пароль должен быть минимум 8 символов, " +
                            "содержать цифры, строчные и заглавные буквы, специальные символы (@#$%^&+=!)")
            );
        }
    }
}