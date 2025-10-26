package com.fitnessclub.service;

import com.fitnessclub.model.Trainer;
import com.fitnessclub.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public Optional<Trainer> getTrainerById(Long id) {
        return trainerRepository.findById(id);
    }

    public Trainer createTrainer(Trainer trainer) {
        if (trainerRepository.existsByEmail(trainer.getEmail())) {
            throw new RuntimeException("Trainer with this email already exists");
        }
        return trainerRepository.save(trainer);
    }

    public Trainer updateTrainer(Long id, Trainer trainerDetails) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        trainer.setFirstName(trainerDetails.getFirstName());
        trainer.setLastName(trainerDetails.getLastName());
        trainer.setSpecialization(trainerDetails.getSpecialization());
        trainer.setPhoneNumber(trainerDetails.getPhoneNumber());

        return trainerRepository.save(trainer);
    }

    public void deleteTrainer(Long id) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        trainerRepository.delete(trainer);
    }
}