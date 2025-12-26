package com.fitnessclub.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для получения CSRF токена
 * Используется в ветке demo/csrf-enabled
 */
@RestController
@RequestMapping("/api")
public class CsrfController {

    @GetMapping("/csrf-token")
    public Map<String, String> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        Map<String, String> response = new HashMap<>();
        if (csrfToken != null) {
            response.put("token", csrfToken.getToken());
            response.put("headerName", csrfToken.getHeaderName());
            response.put("parameterName", csrfToken.getParameterName());
        }
        return response;
    }
}

