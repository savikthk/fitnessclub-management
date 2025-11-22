package com.fitnessclub.repository;

import com.fitnessclub.model.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findByMemberId(Long memberId);
    List<TrainingSession> findByTrainerId(Long trainerId);

    @Query("SELECT COUNT(t) > 0 FROM TrainingSession t WHERE t.trainer.id = :trainerId AND t.sessionDate BETWEEN :start AND :end")
    boolean existsByTrainerIdAndSessionDateBetween(@Param("trainerId") Long trainerId,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    List<TrainingSession> findBySessionDateBetween(LocalDateTime start, LocalDateTime end);

    // Метод для поиска пропущенных тренировок
    List<TrainingSession> findBySessionDateBeforeAndStatus(LocalDateTime dateTime, TrainingSession.TrainingStatus status);
}