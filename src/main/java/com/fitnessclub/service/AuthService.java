package com.fitnessclub.service;

import com.fitnessclub.dto.JwtResponse;
import com.fitnessclub.dto.LoginRequest;
import com.fitnessclub.dto.RefreshTokenRequest;
import com.fitnessclub.dto.SignupRequest;
import com.fitnessclub.model.SessionStatus;
import com.fitnessclub.model.ERole;
import com.fitnessclub.model.Role;
import com.fitnessclub.model.User;
import com.fitnessclub.model.UserSession;
import com.fitnessclub.repository.RoleRepository;
import com.fitnessclub.repository.UserSessionRepository;
import com.fitnessclub.repository.UserRepository;
import com.fitnessclub.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserSessionRepository userSessionRepository;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        com.fitnessclub.service.UserDetailsImpl userDetails =
                (com.fitnessclub.service.UserDetailsImpl) authentication.getPrincipal();

        // Ограничим количество активных сессий, например, 5
        long activeSessions = userSessionRepository.countByUserIdAndStatusAndExpiresAtAfter(
                userDetails.getId(), SessionStatus.ACTIVE, LocalDateTime.now());
        if (activeSessions >= 5) {
            throw new RuntimeException("Too many active sessions");
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Вначале создаём пустую сессию, потом заполняем токен и expiry
        UserSession session = new UserSession();
        session.setUser(user);
        // expiresAt пока заполним после валидации refresh токена
        userSessionRepository.save(session);

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails, session.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, session.getId());

        // Валидация refresh-токена, чтобы вытащить дату истечения
        var parsedRefresh = jwtTokenProvider.validateRefreshToken(refreshToken);

        session.setRefreshToken(refreshToken);
        session.setStatus(SessionStatus.ACTIVE);
        session.setExpiresAt(jwtTokenProvider.getExpiration(parsedRefresh));
        userSessionRepository.save(session);

        return new JwtResponse(accessToken, refreshToken);
    }

    public JwtResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Invalid refresh token");
        }

        UserSession session = sessionOpt.get();

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Session is not active");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            session.setRevokedAt(LocalDateTime.now());
            userSessionRepository.save(session);
            throw new RuntimeException("Session has expired");
        }

        // Проверяем подпись и тип токена
        var parsed = jwtTokenProvider.validateRefreshToken(refreshToken);

        Long userId = parsed.getBody().get("userId", Long.class);
        Long sessionId = parsed.getBody().get("sessionId", Long.class);

        if (!session.getId().equals(sessionId) || !session.getUser().getId().equals(userId)) {
            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(LocalDateTime.now());
            userSessionRepository.save(session);
            throw new RuntimeException("Refresh token does not match session");
        }

        // После одного использования refresh токен должен стать недействительным
        session.setStatus(SessionStatus.REPLACED);
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);

        User user = session.getUser();
        com.fitnessclub.service.UserDetailsImpl userDetails =
                com.fitnessclub.service.UserDetailsImpl.build(user);

        // Создаём новую сессию и пару токенов
        UserSession newSession = new UserSession();
        newSession.setUser(user);
        userSessionRepository.save(newSession);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails, newSession.getId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails, newSession.getId());

        var parsedNewRefresh = jwtTokenProvider.validateRefreshToken(newRefreshToken);
        newSession.setRefreshToken(newRefreshToken);
        newSession.setStatus(SessionStatus.ACTIVE);
        newSession.setExpiresAt(jwtTokenProvider.getExpiration(parsedNewRefresh));
        newSession.setReplacedBySessionId(null);
        userSessionRepository.save(newSession);

        // Связываем старую и новую сессию
        session.setReplacedBySessionId(newSession.getId());
        userSessionRepository.save(session);

        return new JwtResponse(newAccessToken, newRefreshToken);
    }

    public void registerUser(SignupRequest signupRequest) {
        // Проверяем уникальность username и email
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Проверяем надежность пароля
        if (!isPasswordValid(signupRequest.getPassword())) {
            throw new RuntimeException("Error: Password must be at least 8 characters long, " +
                    "contain at least one digit, one lowercase, one uppercase letter, " +
                    "and one special character!");
        }

        // Создаем нового пользователя
        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        // Назначаем роли
        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // По умолчанию назначаем роль MEMBER
            Role memberRole = roleRepository.findByName(ERole.ROLE_MEMBER)
                    .orElseThrow(() -> new RuntimeException("Error: Role MEMBER is not found."));
            roles.add(memberRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
                        roles.add(adminRole);
                        break;
                    case "trainer":
                        Role trainerRole = roleRepository.findByName(ERole.ROLE_TRAINER)
                                .orElseThrow(() -> new RuntimeException("Error: Role TRAINER is not found."));
                        roles.add(trainerRole);
                        break;
                    default:
                        Role memberRole = roleRepository.findByName(ERole.ROLE_MEMBER)
                                .orElseThrow(() -> new RuntimeException("Error: Role MEMBER is not found."));
                        roles.add(memberRole);
                }
            });
        }

        user.setRoles(roles);

        // Связываем с существующими сущностями, если указаны ID
        if (signupRequest.getMemberId() != null) {
            // Здесь нужно получить Member по ID и установить связь
            // Для этого потребуется MemberRepository
        }

        if (signupRequest.getTrainerId() != null) {
            // Здесь нужно получить Trainer по ID и установить связь
            // Для этого потребуется TrainerRepository
        }

        userRepository.save(user);
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}