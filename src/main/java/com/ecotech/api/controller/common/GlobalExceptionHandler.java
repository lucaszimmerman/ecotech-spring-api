package com.ecotech.api.controller.common;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.ecotech.api.controller.dto.ErroCampo;
import com.ecotech.api.controller.dto.ErroResposta;
import com.ecotech.api.exceptions.CampoInvalidoException;
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
        log.warn("Erro de validação: {}", e.getMessage());
        List<ErroCampo> errorsList = e.getFieldErrors()
                .stream()
                .map(fe -> new ErroCampo(fe.getField(), fe.getDefaultMessage())).toList();
                return new ErroResposta(
                    HttpStatus.UNPROCESSABLE_CONTENT.value(),
                    "Erro de validação.",
                    errorsList);
    }

    @ExceptionHandler(RegistroDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResposta handleRegistroDuplicadoException(RegistroDuplicadoException e){
        return ErroResposta.conflito(e.getMessage());
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta handleOperacaoNaoPermitidaException(OperacaoNaoPermitidaException e){
        return ErroResposta.respostaPadrao(e.getMessage());
    }


    @ExceptionHandler(CampoInvalidoException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErroResposta handleCampoInvalidoException(CampoInvalidoException e){
        return new ErroResposta(
            HttpStatus.UNPROCESSABLE_CONTENT.value(),
            "Erro de validação.",
            List.of(new ErroCampo(e.getCampo(), e.getMessage()))
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErroResposta handleAccessDeniedException(AccessDeniedException e){
        return new ErroResposta(
            HttpStatus.FORBIDDEN.value(),
            "Acesso negado.",
            List.of()
        );
    }

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResposta handleRegistroNaoEncontradoException(RegistroNaoEncontradoException e){
        return new ErroResposta(
            HttpStatus.NOT_FOUND.value(),
            e.getMessage(),
            List.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ErroResposta handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e) {

    if (e.getRequiredType() == UUID.class) {
        return ErroResposta.respostaPadrao(
                "O identificador informado é inválido."
        );
    }

    return ErroResposta.respostaPadrao(
            "Parâmetro inválido."
    );
}

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResposta handleErrosNaoTratados(RuntimeException e){
        log.error("Erro inesperado", e);
        return new ErroResposta(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ocorreu um erro inesperado.", List.of());
    }

}
