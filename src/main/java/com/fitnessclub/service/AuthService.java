package com.fitnessclub.service;

import com.fitnessclub.dto.SignupRequest;
import com.fitnessclub.model.ERole;
import com.fitnessclub.model.Role;
import com.fitnessclub.model.User;
import com.fitnessclub.repository.RoleRepository;
import com.fitnessclub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Регулярное выражение для проверки пароля
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    public void registerUser(SignupRequest signupRequest) {
        // 1. Проверяем уникальность username
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Ошибка: Имя пользователя уже занято!");
        }

        // 2. Проверяем уникальность email
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Ошибка: Email уже используется!");
        }

        // 3. Проверяем надежность пароля
        if (!isPasswordValid(signupRequest.getPassword())) {
            throw new RuntimeException("Ошибка: Пароль должен быть минимум 8 символов, " +
                    "содержать цифры, строчные и заглавные буквы, специальные символы (@#$%^&+=!)!");
        }

        // 4. Создаем нового пользователя
        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        // 5. Назначаем роли
        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // По умолчанию назначаем роль MEMBER
            Role memberRole = roleRepository.findByName(ERole.ROLE_MEMBER)
                    .orElseGet(() -> {
                        // Если роль не найдена, создаем ее
                        Role newRole = new Role(ERole.ROLE_MEMBER);
                        return roleRepository.save(newRole);
                    });
            roles.add(memberRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseGet(() -> {
                                    Role newRole = new Role(ERole.ROLE_ADMIN);
                                    return roleRepository.save(newRole);
                                });
                        roles.add(adminRole);
                        break;
                    case "trainer":
                        Role trainerRole = roleRepository.findByName(ERole.ROLE_TRAINER)
                                .orElseGet(() -> {
                                    Role newRole = new Role(ERole.ROLE_TRAINER);
                                    return roleRepository.save(newRole);
                                });
                        roles.add(trainerRole);
                        break;
                    default:
                        Role memberRole = roleRepository.findByName(ERole.ROLE_MEMBER)
                                .orElseGet(() -> {
                                    Role newRole = new Role(ERole.ROLE_MEMBER);
                                    return roleRepository.save(newRole);
                                });
                        roles.add(memberRole);
                }
            });
        }

        user.setRoles(roles);

        // 6. Сохраняем пользователя в БД
        userRepository.save(user);
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    // Дополнительный метод для проверки пароля
    public boolean validatePassword(String password) {
        return isPasswordValid(password);
    }
}