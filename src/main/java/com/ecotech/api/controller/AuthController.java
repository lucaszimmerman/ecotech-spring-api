package com.ecotech.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import com.ecotech.api.controller.dto.CreateUserDTO;
import com.ecotech.api.controller.dto.auth.ForgotPasswordDTO;
import com.ecotech.api.controller.dto.auth.LoginRequestDTO;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.controller.dto.auth.ResetPasswordDTO;
import com.ecotech.api.service.AuthenticationService;
import com.ecotech.api.service.EmailVerificationService;
import com.ecotech.api.service.PasswordRecoveryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO dto) {
        LoginResponseDTO response = authenticationService.login(dto);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(
            @RequestBody @Valid CreateUserDTO dto) {
        LoginResponseDTO response = authenticationService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token) {

        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(
                "Email confirmado com sucesso.");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        emailVerificationService.resendVerification(authenticatedUserId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @RequestBody @Valid ForgotPasswordDTO dto) {

        passwordRecoveryService.requestPasswordReset(dto.email());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordDTO dto) {

        passwordRecoveryService.resetPassword(dto);

        return ResponseEntity.noContent().build();
    }
}
