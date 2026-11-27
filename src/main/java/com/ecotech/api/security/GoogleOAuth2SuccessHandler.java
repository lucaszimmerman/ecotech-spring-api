package com.ecotech.api.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecotech.api.config.FrontendProperties;
import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.exceptions.OperacaoNaoPermitidaException;
import com.ecotech.api.service.GoogleAuthenticationService;
import com.ecotech.api.service.GoogleAuthenticationService.GoogleLoginResult;
import com.ecotech.api.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Conclui o login Google redirecionando o navegador para o frontend com um
 * codigo de uso unico. O frontend troca o codigo pelo JWT em
 * POST /auth/google/exchange.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final GoogleAuthenticationService googleAuthenticationService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final GoogleLoginCodeStore codeStore;
    private final FrontendProperties frontendProperties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OidcUser oidcUser =
                (OidcUser) authentication.getPrincipal();

        GoogleLoginResult result;

        try {
            result = googleAuthenticationService
                    .authenticateForLogin(oidcUser);
        } catch (OperacaoNaoPermitidaException exception) {
            log.warn("Login Google recusado no success handler: {}", exception.getMessage());
            response.sendRedirect(
                    GoogleOAuth2FailureHandler.errorRedirect(
                            frontendProperties, exception.getMessage()));
            return;
        }

        UserPrincipal principal =
                new UserPrincipal(result.user());

        LoginResponseDTO loginResponse =
                new LoginResponseDTO(
                        principal.getId(),
                        principal.getUsername(),
                        principal.getName(),
                        principal.getRole(),
                        jwtService.generateToken(principal),
                        "Bearer",
                        jwtProperties.expiration()
                );

        UriComponentsBuilder redirect = UriComponentsBuilder
                .fromUriString(frontendProperties.callbackUrl())
                .queryParam("code", codeStore.issue(loginResponse));

        if (result.created()) {
            redirect.queryParam("novo", "1");
        }

        response.sendRedirect(redirect.build().encode().toUriString());

        log.info("Login Google concluido, redirecionando ao frontend. userId={}", principal.getId());
    }
}
