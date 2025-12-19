package com.fitnessclub.config;

import com.fitnessclub.model.ERole;
import com.fitnessclub.model.Role;
import com.fitnessclub.model.User;
import com.fitnessclub.repository.RoleRepository;
import com.fitnessclub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Создаем роли, если они не существуют
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            roleRepository.save(new Role(ERole.ROLE_MEMBER));
            roleRepository.save(new Role(ERole.ROLE_TRAINER));
            System.out.println("Roles initialized");
        }

        // Создаем администратора по умолчанию, если его нет
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@fitnessclub.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("Default admin user created");
            System.out.println("Username: admin");
            System.out.println("Password: Admin123!");
        }
    }
}