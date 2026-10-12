package com.parcelflow.parcelflow.service;

import com.parcelflow.parcelflow.domain.User;
import com.parcelflow.parcelflow.domain.UserRole;
import com.parcelflow.parcelflow.dto.LoginRequest;
import com.parcelflow.parcelflow.dto.LoginResponse;
import com.parcelflow.parcelflow.dto.RegisterRequest;
import com.parcelflow.parcelflow.repository.UserRepository;
import com.parcelflow.parcelflow.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalStateException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());

        String hashedPassword = passwordEncoder.encode(request.password());
        user.setPassword(hashedPassword);
        user.setRole(UserRole.USER);

        userRepository.save(user);
    }
    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                request.username(),
                                request.password()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("User has no role")
                );

        return new LoginResponse(token, role);
    }
}