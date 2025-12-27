package com.fitnessclub.service;

import com.fitnessclub.dto.TrainingSessionResponse;
import com.fitnessclub.model.TrainingSession;
import com.fitnessclub.model.Member;
import com.fitnessclub.model.Trainer;
import com.fitnessclub.repository.TrainingSessionRepository;
import com.fitnessclub.repository.MemberRepository;
import com.fitnessclub.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrainingSessionService {

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    // Бизнес-операция 1: Запись на персональную тренировку
    public TrainingSessionResponse bookTrainingSession(Long memberId, Long trainerId,
                                                       LocalDateTime sessionDate, Integer duration) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new RuntimeException("Trainer not found with id: " + trainerId));

        LocalDateTime sessionStart = sessionDate.minusMinutes(30);
        LocalDateTime sessionEnd = sessionDate.plusMinutes(duration).plusMinutes(30);

        boolean isTrainerBusy = trainingSessionRepository.existsByTrainerIdAndSessionDateBetween(
                trainerId, sessionStart, sessionEnd);

        if (isTrainerBusy) {
            throw new RuntimeException("Trainer is not available at this time");
        }

        TrainingSession trainingSession = new TrainingSession(member, trainer, sessionDate, duration);
        TrainingSession savedSession = trainingSessionRepository.save(trainingSession);
        return new TrainingSessionResponse(savedSession);
    }

    // Бизнес-операция 5: Уведомление о пропущенных тренировках
    public List<TrainingSessionResponse> findMissedTrainingSessions() {
        LocalDateTime now = LocalDateTime.now();

        // Находим запланированные тренировки, которые уже прошли
        List<TrainingSession> missedSessions = trainingSessionRepository
                .findBySessionDateBeforeAndStatus(now, TrainingSession.TrainingStatus.SCHEDULED);

        // Меняем статус на NO_SHOW
        missedSessions.forEach(session -> {
            session.setStatus(TrainingSession.TrainingStatus.NO_SHOW);
            trainingSessionRepository.save(session);
        });

        return missedSessions.stream()
                .map(TrainingSessionResponse::new)
                .collect(Collectors.toList());
    }

    public List<TrainingSessionResponse> getMemberSessions(Long memberId) {
        return trainingSessionRepository.findByMemberId(memberId)
                .stream()
                .map(TrainingSessionResponse::new)
                .collect(Collectors.toList());
    }

    public List<TrainingSessionResponse> getTrainerSessions(Long trainerId) {
        return trainingSessionRepository.findByTrainerId(trainerId)
                .stream()
                .map(TrainingSessionResponse::new)
                .collect(Collectors.toList());
    }

    public void cancelTrainingSession(Long sessionId) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Training session not found"));

        if (!session.canBeCancelled()) {
            throw new RuntimeException("Training session cannot be cancelled");
        }

        session.setStatus(TrainingSession.TrainingStatus.CANCELLED);
        trainingSessionRepository.save(session);
    }

    public List<TrainingSessionResponse> getAllSessions() {
        return trainingSessionRepository.findAll()
                .stream()
                .map(TrainingSessionResponse::new)
                .collect(Collectors.toList());
    }

    public TrainingSessionResponse updateTrainingSession(Long sessionId, Long trainerId,
                                                          String sessionDate, Integer duration, String status) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Training session not found with id: " + sessionId));

        if (trainerId != null && !trainerId.equals(session.getTrainer().getId())) {
            Trainer newTrainer = trainerRepository.findById(trainerId)
                    .orElseThrow(() -> new RuntimeException("Trainer not found with id: " + trainerId));
            session.setTrainer(newTrainer);
        }

        if (sessionDate != null) {
            LocalDateTime newDate = LocalDateTime.parse(sessionDate);
            session.setSessionDate(newDate);
        }

        if (duration != null) {
            session.setDuration(duration);
        }

        if (status != null) {
            try {
                TrainingSession.TrainingStatus statusEnum = TrainingSession.TrainingStatus.valueOf(status.toUpperCase());
                session.setStatus(statusEnum);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value: " + status);
            }
        }

        TrainingSession savedSession = trainingSessionRepository.save(session);
        return new TrainingSessionResponse(savedSession);
    }

    private boolean isTrainerBusy(Long trainerId, LocalDateTime sessionDate) {
        LocalDateTime sessionStart = sessionDate.minusMinutes(30);
        LocalDateTime sessionEnd = sessionDate.plusMinutes(60);
        return trainingSessionRepository.existsByTrainerIdAndSessionDateBetween(
                trainerId, sessionStart, sessionEnd);
    }
}