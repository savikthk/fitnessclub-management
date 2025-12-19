package com.fitnessclub.security;

import com.fitnessclub.model.User;
import com.fitnessclub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    @Autowired
    private UserRepository userRepository;

    public boolean isMemberOwner(Long memberId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, связан ли пользователь с этим memberId
        return user.getMember() != null && user.getMember().getId().equals(memberId);
    }

    public boolean isTrainerOwner(Long trainerId, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, связан ли пользователь с этим trainerId
        return user.getTrainer() != null && user.getTrainer().getId().equals(trainerId);
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}