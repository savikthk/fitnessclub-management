package com.fitnessclub.controller;

import com.fitnessclub.model.WorkoutClass;
import com.fitnessclub.service.WorkoutClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class WorkoutClassController {
    private final WorkoutClassService workoutClassService;

    @Autowired
    public WorkoutClassController(WorkoutClassService workoutClassService) {
        this.workoutClassService = workoutClassService;
    }

    @GetMapping
    public List<WorkoutClass> getAllClasses() {
        return workoutClassService.getAllClasses();
    }

    @GetMapping("/upcoming")
    public List<WorkoutClass> getUpcomingClasses() {
        return workoutClassService.getUpcomingClasses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutClass> getClassById(@PathVariable Long id) {
        return workoutClassService.getClassById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public WorkoutClass createClass(@RequestBody WorkoutClass workoutClass) {
        return workoutClassService.createClass(workoutClass);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutClass> updateClass(@PathVariable Long id, @RequestBody WorkoutClass classDetails) {
        try {
            WorkoutClass updatedClass = workoutClassService.updateClass(id, classDetails);
            return ResponseEntity.ok(updatedClass);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
        try {
            workoutClassService.deleteClass(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}