package com.ecotech.api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.controller.dto.auth.LoginRequestDTO;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        dto.username(),
                        dto.password()
                );

        Authentication authenticationResponse =
                authenticationManager.authenticate(authenticationRequest);

        UserPrincipal principal =
                (UserPrincipal) authenticationResponse.getPrincipal();

        String accessToken = jwtService.generateToken(principal);

        return new LoginResponseDTO(
                principal.getId(),
                principal.getUsername(),
                principal.getName(),
                principal.getRole(),
                accessToken,
                "Bearer",
                jwtProperties.expiration()
        );
    }
}
