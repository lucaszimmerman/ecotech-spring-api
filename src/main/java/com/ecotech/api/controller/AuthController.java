package com.ecotech.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecotech.api.controller.dto.auth.LoginRequestDTO;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO){

        Authentication authenticationRequest = 
        UsernamePasswordAuthenticationToken.unauthenticated(
            loginRequestDTO.username(),
             loginRequestDTO.password()
        ); 

        Authentication authenticationResponse =
         authenticationManager.authenticate(authenticationRequest);

        UserPrincipal userPrincipal =
         (UserPrincipal) authenticationResponse.getPrincipal();

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(
            userPrincipal.getId(),
            userPrincipal.getUsername(),
            userPrincipal.getName(),
            userPrincipal.getRole()
        );

        return ResponseEntity.ok(loginResponseDTO);
    }
    }
