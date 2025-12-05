package com.fitnessclub.service;

import com.fitnessclub.dto.SignupRequest;
import com.fitnessclub.model.ERole;
import com.fitnessclub.model.Role;
import com.fitnessclub.model.SessionStatus;
import com.fitnessclub.model.User;
import com.fitnessclub.model.UserSession;
import com.fitnessclub.repository.RoleRepository;
import com.fitnessclub.repository.UserRepository;
import com.fitnessclub.repository.UserSessionRepository;
import com.fitnessclub.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Регулярное выражение для проверки пароля
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    /**
     * Регистрация нового пользователя
     */
    public void registerUser(SignupRequest signupRequest) {
        // 1. Проверяем уникальность username
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Ошибка: Имя пользователя уже занято!");
        }

        // 2. Проверяем уникальность email
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Ошибка: Email уже используется!");
        }

        // 3. Проверяем надежность пароля
        if (!isPasswordValid(signupRequest.getPassword())) {
            throw new RuntimeException("Ошибка: Пароль должен быть минимум 8 символов, " +
                    "содержать цифры, строчные и заглавные буквы, специальные символы (@#$%^&+=!)!");
        }

        // 4. Создаем нового пользователя
        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        // 5. Назначаем роли
        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // По умолчанию назначаем роль MEMBER
            Role memberRole = roleRepository.findByName(ERole.ROLE_MEMBER)
                    .orElseGet(() -> {
                        // Если роль не найдена, создаем ее
                        Role newRole = new Role(ERole.ROLE_MEMBER);
                        return roleRepository.save(newRole);
                    });
            roles.add(memberRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseGet(() -> {
                                    Role newRole = new Role(ERole.ROLE_ADMIN);
                                    return roleRepository.save(newRole);
                                });
                        roles.add(adminRole);
                        break;
                    case "trainer":
                        Role trainerRole = roleRepository.findByName(ERole.ROLE_TRAINER)
                                .orElseGet(() -> {
                                    Role newRole = new Role(ERole.ROLE_TRAINER);
                                    return roleRepository.save(newRole);
                                });
                        roles.add(trainerRole);
                        break;
                    default:
                        Role memberRole = roleRepository.findByName(ERole.ROLE_MEMBER)
                                .orElseGet(() -> {
                                    Role newRole = new Role(ERole.ROLE_MEMBER);
                                    return roleRepository.save(newRole);
                                });
                        roles.add(memberRole);
                }
            });
        }

        user.setRoles(roles);

        // 6. Сохраняем пользователя в БД
        userRepository.save(user);
    }

    /**
     * Аутентификация пользователя и генерация токенов
     */
    public TokenPair authenticateUser(String username, String password, HttpServletRequest request) {
        try {
            // Аутентификация через Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Получаем пользователя из БД
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Получаем роли пользователя
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_MEMBER");

            // Убираем префикс ROLE_
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }

            // Генерируем пару токенов
            return generateTokenPair(user.getId(), username, role, request);

        } catch (AuthenticationException e) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }
    }

    /**
     * Генерация пары токенов (access + refresh)
     */
    public TokenPair generateTokenPair(Long userId, String username, String role,
                                       HttpServletRequest request) {
        // Создаем новую сессию
        UserSession session = new UserSession();
        session.setUserId(userId);

        // Генерируем refresh token
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, username, session.getSessionId());
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));

        // Сохраняем информацию о клиенте
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setIpAddress(getClientIp(request));

        // Сохраняем сессию
        userSessionRepository.save(session);

        // Генерируем access token
        String accessToken = jwtTokenProvider.generateAccessToken(userId, username, role, session.getSessionId());

        return new TokenPair(accessToken, refreshToken, session.getSessionId());
    }

    /**
     * Обновление пары токенов
     */
    public TokenPair refreshTokens(String oldRefreshToken, HttpServletRequest request) {
        // Проверяем валидность refresh token
        if (!jwtTokenProvider.validateRefreshToken(oldRefreshToken)) {
            throw new SecurityException("Недействительный refresh token");
        }

        // Находим активную сессию
        Optional<UserSession> sessionOpt = userSessionRepository.findActiveByRefreshToken(
                oldRefreshToken, LocalDateTime.now());

        if (sessionOpt.isEmpty()) {
            throw new SecurityException("Refresh token не найден или истек");
        }

        UserSession oldSession = sessionOpt.get();

        // Помечаем старую сессию как замененную
        userSessionRepository.updateStatus(oldSession.getSessionId(), SessionStatus.REPLACED);

        // Получаем данные из токена
        Long userId = jwtTokenProvider.getUserIdFromToken(oldRefreshToken, JwtTokenProvider.TokenType.REFRESH);
        String username = jwtTokenProvider.getUsernameFromToken(oldRefreshToken, JwtTokenProvider.TokenType.REFRESH);

        // Получаем пользователя и его роль
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String role = user.getRoles().stream()
                .map(r -> r.getName().name())
                .findFirst()
                .orElse("MEMBER");

        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        // Генерируем новую пару токенов
        return generateTokenPair(userId, username, role, request);
    }

    /**
     * Выход из системы
     */
    public void logout(String refreshToken) {
        userSessionRepository.findByRefreshTokenAndStatus(refreshToken, SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    userSessionRepository.updateStatus(session.getSessionId(), SessionStatus.LOGGED_OUT);
                });
    }

    /**
     * Выход из всех устройств
     */
    public void logoutAll(Long userId) {
        userSessionRepository.revokeAllUserSessions(userId);
    }

    /**
     * Проверка валидности access токена
     */
    public boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateAccessToken(token);
    }

    /**
     * Получение userId из access токена
     */
    public Long getUserIdFromAccessToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token, JwtTokenProvider.TokenType.ACCESS);
    }

    /**
     * Получение пользователя из access токена
     */
    public User getUserFromAccessToken(String token) {
        Long userId = getUserIdFromAccessToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    // Дополнительный метод для проверки пароля
    public boolean validatePassword(String password) {
        return isPasswordValid(password);
    }

    /**
     * Получение IP адреса клиента
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * DTO для пары токенов
     */
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final String sessionId;
        private final long accessTokenExpiresIn;
        private final long refreshTokenExpiresIn;

        public TokenPair(String accessToken, String refreshToken, String sessionId) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.sessionId = sessionId;
            this.accessTokenExpiresIn = 15 * 60; // 15 минут в секундах
            this.refreshTokenExpiresIn = 7 * 24 * 60 * 60; // 7 дней в секундах
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getSessionId() { return sessionId; }
        public long getAccessTokenExpiresIn() { return accessTokenExpiresIn; }
        public long getRefreshTokenExpiresIn() { return refreshTokenExpiresIn; }
    }
}