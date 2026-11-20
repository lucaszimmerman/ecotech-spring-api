package com.ecotech.api.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostDTO(
    @NotBlank(message = "O conteudo e obrigatorio.")
    @Size(
        min = 5,
        max = 2000,
        message = "O conteudo deve possuir entre 5 e 2000 caracteres."
    )
    String content
) {
}
