// Файл: src/main/java/com/fitnessclub/repository/UserSessionRepository.java
package com.fitnessclub.repository;

import com.fitnessclub.model.UserSession;
import com.fitnessclub.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    Optional<UserSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);

    List<UserSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    Optional<UserSession> findBySessionIdAndStatus(String sessionId, SessionStatus status);

    @Query("SELECT us FROM UserSession us WHERE us.refreshToken = :refreshToken " +
            "AND us.status = 'ACTIVE' AND us.expiresAt > :now")
    Optional<UserSession> findActiveByRefreshToken(@Param("refreshToken") String refreshToken,
                                                   @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession us SET us.status = :status WHERE us.sessionId = :sessionId")
    void updateStatus(@Param("sessionId") String sessionId,
                      @Param("status") SessionStatus status);

    @Modifying
    @Query("UPDATE UserSession us SET us.status = 'REVOKED' " +
            "WHERE us.userId = :userId AND us.status = 'ACTIVE'")
    void revokeAllUserSessions(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);
}