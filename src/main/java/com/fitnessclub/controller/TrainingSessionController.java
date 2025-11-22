package com.fitnessclub.controller;

import com.fitnessclub.dto.TrainingSessionResponse;
import com.fitnessclub.service.TrainingSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/training-sessions")
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    @Autowired
    public TrainingSessionController(TrainingSessionService trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }

    // Бизнес-операция 1: Запись на тренировку
    @PostMapping("/book")
    public ResponseEntity<?> bookTrainingSession(
            @RequestParam Long memberId,
            @RequestParam Long trainerId,
            @RequestParam String sessionDate,
            @RequestParam Integer duration) {

        try {
            LocalDateTime dateTime = LocalDateTime.parse(sessionDate);
            TrainingSessionResponse session = trainingSessionService.bookTrainingSession(
                    memberId, trainerId, dateTime, duration);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Бизнес-операция 5: Поиск пропущенных тренировок
    @GetMapping("/missed")
    public List<TrainingSessionResponse> findMissedTrainingSessions() {
        return trainingSessionService.findMissedTrainingSessions();
    }

    @GetMapping("/member/{memberId}")
    public List<TrainingSessionResponse> getMemberSessions(@PathVariable Long memberId) {
        return trainingSessionService.getMemberSessions(memberId);
    }

    @GetMapping("/trainer/{trainerId}")
    public List<TrainingSessionResponse> getTrainerSessions(@PathVariable Long trainerId) {
        return trainingSessionService.getTrainerSessions(trainerId);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTrainingSession(@PathVariable Long id) {
        try {
            trainingSessionService.cancelTrainingSession(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<TrainingSessionResponse> getAllSessions() {
        return trainingSessionService.getAllSessions();
    }
}