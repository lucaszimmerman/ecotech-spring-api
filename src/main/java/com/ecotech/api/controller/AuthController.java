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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import static com.ecotech.api.config.OpenApiConfiguration.BEARER_AUTH;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
@Hidden
public class AuthController {

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/login")
    @Operation(summary = "Autentica um usuario com email e senha")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais invalidas"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO dto) {
        LoginResponseDTO response = authenticationService.login(dto);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado com sucesso"),
            @ApiResponse(responseCode = "409", description = "Usuario ja cadastrado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<LoginResponseDTO> register(
            @RequestBody @Valid CreateUserDTO dto) {
        LoginResponseDTO response = authenticationService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Confirma o email de um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email confirmado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametro invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "422", description = "Token invalido ou expirado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token) {

        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(
                "Email confirmado com sucesso.");
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Reenvia o email de verificacao", security = @SecurityRequirement(name = BEARER_AUTH))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email de verificacao reenviado"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "422", description = "Email ja verificado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> resendVerification(Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        emailVerificationService.resendVerification(authenticatedUserId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicita recuperacao de senha")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Solicitacao processada"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> forgotPassword(
            @RequestBody @Valid ForgotPasswordDTO dto) {

        passwordRecoveryService.requestPasswordReset(dto.email());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefine a senha usando token de recuperacao")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada ou token invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> resetPassword(
            @RequestBody @Valid ResetPasswordDTO dto) {

        passwordRecoveryService.resetPassword(dto);

        return ResponseEntity.noContent().build();
    }
}
