package com.fitnessclub.service;

import com.fitnessclub.model.WorkoutClass;
import com.fitnessclub.repository.WorkoutClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class WorkoutClassService {
    private final WorkoutClassRepository workoutClassRepository;

    @Autowired
    public WorkoutClassService(WorkoutClassRepository workoutClassRepository) {
        this.workoutClassRepository = workoutClassRepository;
    }

    public List<WorkoutClass> getAllClasses() {
        return workoutClassRepository.findAll();
    }

    public Optional<WorkoutClass> getClassById(Long id) {
        return workoutClassRepository.findById(id);
    }

    public List<WorkoutClass> getUpcomingClasses() {
        return workoutClassRepository.findByClassDateTimeAfter(java.time.LocalDateTime.now());
    }

    public WorkoutClass createClass(WorkoutClass workoutClass) {
        return workoutClassRepository.save(workoutClass);
    }

    public WorkoutClass updateClass(Long id, WorkoutClass classDetails) {
        WorkoutClass workoutClass = workoutClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout class not found"));

        workoutClass.setClassName(classDetails.getClassName());
        workoutClass.setDescription(classDetails.getDescription());
        workoutClass.setClassDateTime(classDetails.getClassDateTime());
        workoutClass.setDurationMinutes(classDetails.getDurationMinutes());
        workoutClass.setMaxCapacity(classDetails.getMaxCapacity());

        return workoutClassRepository.save(workoutClass);
    }

    public void deleteClass(Long id) {
        WorkoutClass workoutClass = workoutClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout class not found"));
        workoutClassRepository.delete(workoutClass);
    }
}