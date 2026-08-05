package com.ecotech.api.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostDTO(
     @NotBlank(message = "O conteúdo é obrigatório.")
    @Size(
        min = 5,
        max = 2000,
        message = "O conteúdo deve possuir entre 5 e 2000 caracteres."
    )
    String content,

    @Size(
        max = 500,
        message = "A URL da imagem deve possuir no máximo 500 caracteres."
    )
    String imageUrl
) {
    
}
