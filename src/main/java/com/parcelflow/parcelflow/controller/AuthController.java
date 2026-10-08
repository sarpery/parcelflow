package com.parcelflow.parcelflow.controller;

import com.parcelflow.parcelflow.domain.User;
import com.parcelflow.parcelflow.dto.LoginRequest;
import com.parcelflow.parcelflow.dto.LoginResponse;
import com.parcelflow.parcelflow.dto.RegisterRequest;
import com.parcelflow.parcelflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){

        String token = authService.login(request);
        return new LoginResponse(token);
    }
}