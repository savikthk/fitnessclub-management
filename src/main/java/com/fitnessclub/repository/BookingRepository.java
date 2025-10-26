package com.fitnessclub.repository;

import com.fitnessclub.model.Booking;
import com.fitnessclub.model.Member;
import com.fitnessclub.model.WorkoutClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByMember(Member member);
    List<Booking> findByWorkoutClass(WorkoutClass workoutClass);
    Optional<Booking> findByMemberAndWorkoutClass(Member member, WorkoutClass workoutClass);
    boolean existsByMemberAndWorkoutClass(Member member, WorkoutClass workoutClass);
}