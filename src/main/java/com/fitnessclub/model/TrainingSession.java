package com.fitnessclub.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_sessions")
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference("member-trainingsessions")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(name = "session_date", nullable = false)
    private LocalDateTime sessionDate;

    @Column(nullable = false)
    private Integer duration; // в минутах

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingStatus status;

    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TrainingStatus.SCHEDULED;
        }
    }

    public boolean canBeCancelled() {
        return status == TrainingStatus.SCHEDULED &&
                sessionDate.minusHours(2).isAfter(LocalDateTime.now());
    }

    public enum TrainingStatus {
        SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    }

    // Конструкторы
    public TrainingSession() {}

    public TrainingSession(Member member, Trainer trainer, LocalDateTime sessionDate, Integer duration) {
        this.member = member;
        this.trainer = trainer;
        this.sessionDate = sessionDate;
        this.duration = duration;
        this.status = TrainingStatus.SCHEDULED;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public Trainer getTrainer() { return trainer; }
    public void setTrainer(Trainer trainer) { this.trainer = trainer; }

    public LocalDateTime getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public TrainingStatus getStatus() { return status; }
    public void setStatus(TrainingStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}