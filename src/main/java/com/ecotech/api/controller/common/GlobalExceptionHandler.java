package com.ecotech.api.controller.common;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.ecotech.api.controller.dto.ErroCampo;
import com.ecotech.api.controller.dto.ErroResposta;
import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.EmailDeliveryException;
import com.ecotech.api.exceptions.OperacaoNaoPermitidaException;
import com.ecotech.api.exceptions.RegistroDuplicadoException;
import com.ecotech.api.exceptions.RegistroNaoEncontradoException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErroResposta handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ErroCampo> errorsList = e.getFieldErrors()
                .stream()
                .map(fe -> new ErroCampo(fe.getField(), fe.getDefaultMessage())).toList();
        log.warn(
                "Erro de validacao em payload. camposInvalidos={}",
                errorsList.stream().map(ErroCampo::campo).toList()
        );
        return new ErroResposta(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Erro de validação.",
                errorsList);
    }

    @ExceptionHandler(RegistroDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResposta handleRegistroDuplicadoException(RegistroDuplicadoException e) {
        log.warn("Conflito de registro: {}", e.getMessage());
        return ErroResposta.conflito(e.getMessage());
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta handleOperacaoNaoPermitidaException(OperacaoNaoPermitidaException e) {
        log.warn("Operacao nao permitida: {}", e.getMessage());
        return ErroResposta.respostaPadrao(e.getMessage());
    }

    @ExceptionHandler(CampoInvalidoException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErroResposta handleCampoInvalidoException(CampoInvalidoException e) {
        log.warn("Campo invalido: campo={}, mensagem={}", e.getCampo(), e.getMessage());
        return new ErroResposta(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Erro de validação.",
                List.of(new ErroCampo(e.getCampo(), e.getMessage())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErroResposta handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Acesso negado: {}", e.getMessage());
        return new ErroResposta(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado.",
                List.of());
    }

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResposta handleRegistroNaoEncontradoException(RegistroNaoEncontradoException e) {
        log.warn("Registro nao encontrado: {}", e.getMessage());
        return new ErroResposta(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {

        if (e.getRequiredType() == UUID.class) {
            log.warn("Parametro UUID invalido: parameter={}", e.getName());
            return ErroResposta.respostaPadrao(
                    "O identificador informado é inválido.");
        }

        log.warn("Parametro invalido: parameter={}", e.getName());
        return ErroResposta.respostaPadrao(
                "Parâmetro inválido.");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErroResposta handleAuthenticationException(
            AuthenticationException exception) {
        log.warn("Falha de autenticacao tratada pelo controller advice.");

        return new ErroResposta(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais inválidas.",
                List.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public ErroResposta handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {
        log.warn("Upload recusado por tamanho maximo excedido: {}", e.getMessage());
        return new ErroResposta(
                HttpStatus.CONTENT_TOO_LARGE.value(),
                "O arquivo enviado excede o tamanho máximo permitido.",
                List.of());
    }

    @ExceptionHandler(EmailDeliveryException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErroResposta handleEmailDeliveryException(
            EmailDeliveryException e) {
        log.warn("Falha conhecida no envio de email: {}", e.getMessage());

        return new ErroResposta(
                HttpStatus.BAD_GATEWAY.value(),
                e.getMessage(),
                List.of());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResposta handleErrosNaoTratados(RuntimeException e) {
        log.error("Erro inesperado", e);
        return new ErroResposta(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ocorreu um erro inesperado.", List.of());
    }

}
