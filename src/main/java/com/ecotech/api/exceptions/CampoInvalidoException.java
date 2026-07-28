package com.ecotech.api.exceptions;

import lombok.Getter;

public class CampoInvalidoException extends RuntimeException {
    @Getter
    private final String campo;

    public CampoInvalidoException(String campo, String message) {
        super(message);
        this.campo = campo;
    }
}