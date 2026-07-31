package com.ecotech.api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.ecotech.api.controller.dto.ErroResposta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class SecurityExceptionHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Executado quando o usuário não está autenticado corretamente.
     *
     * Exemplos:
     * - não enviou token;
     * - token inválido;
     * - token expirado;
     * - assinatura inválida.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        writeResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Autenticação necessária ou token inválido."
        );
    }

    /**
     * Executado quando o usuário está autenticado,
     * mas não possui a permissão necessária.
     *
     * Exemplo:
     * - usuário USER tentando acessar uma rota ADMIN.
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {

        writeResponse(
                response,
                HttpStatus.FORBIDDEN,
                "Você não possui permissão para acessar este recurso."
        );
    }

    private void writeResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        ErroResposta errorResponse = new ErroResposta(
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
