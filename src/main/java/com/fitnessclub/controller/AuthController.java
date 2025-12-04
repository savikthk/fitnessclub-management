package com.fitnessclub.controller;

import com.fitnessclub.dto.MessageResponse;
import com.fitnessclub.dto.SignupRequest;
import com.fitnessclub.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        try {
            authService.registerUser(signupRequest);
            return ResponseEntity.ok(new MessageResponse("Пользователь успешно зарегистрирован!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser() {
        return ResponseEntity.ok(new MessageResponse("Аутентификация через Basic Auth успешна"));
    }

    // Эндпоинт для проверки пароля (опционально)
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