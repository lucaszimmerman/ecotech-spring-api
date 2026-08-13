package com.ecotech.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecotech.api.controller.dto.CreateUserDTO;
import com.ecotech.api.controller.dto.auth.LoginRequestDTO;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
@Hidden
public class AuthController {

    private final AuthenticationService authenticationService;

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
}
