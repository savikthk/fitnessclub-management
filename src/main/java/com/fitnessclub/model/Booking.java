package com.fitnessclub.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private WorkoutClass workoutClass;

    @Column(nullable = false)
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @PrePersist
    protected void onCreate() {
        bookingTime = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.CONFIRMED;
        }
    }

    public boolean canBeCancelled() {
        return status == BookingStatus.CONFIRMED &&
                workoutClass.isUpcoming() &&
                workoutClass.getClassDateTime().minusHours(2).isAfter(LocalDateTime.now());
    }

    public enum BookingStatus {
        CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public WorkoutClass getWorkoutClass() { return workoutClass; }
    public void setWorkoutClass(WorkoutClass workoutClass) { this.workoutClass = workoutClass; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}