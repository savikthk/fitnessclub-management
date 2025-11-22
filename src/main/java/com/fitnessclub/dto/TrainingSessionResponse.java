package com.fitnessclub.dto;

import com.fitnessclub.model.TrainingSession;
import java.time.LocalDateTime;

public class TrainingSessionResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long trainerId;
    private String trainerName;
    private LocalDateTime sessionDate;
    private Integer duration;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    public TrainingSessionResponse(TrainingSession trainingSession) {
        this.id = trainingSession.getId();
        this.memberId = trainingSession.getMember().getId();
        this.memberName = trainingSession.getMember().getFirstName() + " " + trainingSession.getMember().getLastName();
        this.trainerId = trainingSession.getTrainer().getId();
        this.trainerName = trainingSession.getTrainer().getFirstName() + " " + trainingSession.getTrainer().getLastName();
        this.sessionDate = trainingSession.getSessionDate();
        this.duration = trainingSession.getDuration();
        this.status = trainingSession.getStatus().name();
        this.notes = trainingSession.getNotes();
        this.createdAt = trainingSession.getCreatedAt();
    }

    // Геттеры
    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public Long getTrainerId() { return trainerId; }
    public String getTrainerName() { return trainerName; }
    public LocalDateTime getSessionDate() { return sessionDate; }
    public Integer getDuration() { return duration; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
