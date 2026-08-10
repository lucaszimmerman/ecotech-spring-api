package com.ecotech.api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecotech.api.controller.dto.ErroResposta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth2FailureHandler
        implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

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

        writeError(
                response,
                HttpStatus.UNAUTHORIZED,
                "Nao foi possivel autenticar com Google."
        );
    }

    public void writeError(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        ErroResposta errorResponse =
                new ErroResposta(
                        status.value(),
                        message,
                        List.of()
                );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}
