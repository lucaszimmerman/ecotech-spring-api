package com.ecotech.api.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDTO(
    @NotBlank(message = "campo obrigatorio")
    @Size(min= 3, max = 30, message = "campo fora do tamanho padrão")
    String username,
    @NotBlank(message = "campo obrigatorio")
    @Email
    @Size(min= 10, max = 150, message = "campo fora do tamanho padrão")
    String email,
    @NotBlank(message = "campo obrigatorio")
    @Size(min= 6, max = 72, message = "campo fora do tamanho padrão")
    String password,
    @NotBlank(message = "campo obrigatorio")
    @Size(min= 2, max = 100, message = "campo fora do tamanho padrão")
    String name
) {}