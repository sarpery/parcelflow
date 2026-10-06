package com.parcelflow.parcelflow.service;

import com.parcelflow.parcelflow.domain.User;
import com.parcelflow.parcelflow.domain.UserRole;
import com.parcelflow.parcelflow.dto.RegisterRequest;
import com.parcelflow.parcelflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalStateException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());

        String hashedPassword = passwordEncoder.encode(request.password());
        user.setPassword(hashedPassword);
        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }
}