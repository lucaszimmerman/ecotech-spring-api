package com.ecotech.api.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.exceptions.OperacaoNaoPermitidaException;
import com.ecotech.api.model.User;
import com.ecotech.api.service.GoogleAuthenticationService;
import com.ecotech.api.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final GoogleAuthenticationService googleAuthenticationService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;
    private final GoogleOAuth2FailureHandler googleOAuth2FailureHandler;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OidcUser oidcUser =
                (OidcUser) authentication.getPrincipal();

        User user;

        try {
            user = googleAuthenticationService
                    .authenticate(oidcUser);
        } catch (OperacaoNaoPermitidaException exception) {
            googleOAuth2FailureHandler.writeError(
                    response,
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
            return;
        }

        UserPrincipal principal =
                new UserPrincipal(user);

        String accessToken =
                jwtService.generateToken(principal);

        LoginResponseDTO loginResponse =
                new LoginResponseDTO(
                        principal.getId(),
                        principal.getUsername(),
                        principal.getName(),
                        principal.getRole(),
                        accessToken,
                        "Bearer",
                        jwtProperties.expiration()
                );

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                loginResponse
        );
    }
}
