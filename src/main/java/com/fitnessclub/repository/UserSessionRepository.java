package com.fitnessclub.repository;

import com.fitnessclub.model.SessionStatus;
import com.fitnessclub.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    long countByUserIdAndStatusAndExpiresAtAfter(Long userId, SessionStatus status, LocalDateTime now);
}







