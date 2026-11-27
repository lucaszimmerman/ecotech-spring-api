package com.ecotech.api.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecotech.api.config.FrontendProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Em caso de falha ou cancelamento no Google, devolve o navegador ao frontend
 * com uma mensagem em {@code ?erro=}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth2FailureHandler
        implements AuthenticationFailureHandler {

    private final FrontendProperties frontendProperties;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        log.warn(
                "Falha no login com Google: {}",
                exception.getMessage()
        );

        response.sendRedirect(errorRedirect(
                frontendProperties,
                "Nao foi possivel autenticar com Google."));
    }

    static String errorRedirect(FrontendProperties frontend, String message) {
        return UriComponentsBuilder
                .fromUriString(frontend.callbackUrl())
                .queryParam("erro", message)
                .build()
                .encode()
                .toUriString();
    }
}
