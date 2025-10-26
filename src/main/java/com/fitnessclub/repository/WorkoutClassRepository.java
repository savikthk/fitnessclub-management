package com.fitnessclub.repository;

import com.fitnessclub.model.WorkoutClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkoutClassRepository extends JpaRepository<WorkoutClass, Long> {
    List<WorkoutClass> findByClassDateTimeAfter(LocalDateTime dateTime);
}