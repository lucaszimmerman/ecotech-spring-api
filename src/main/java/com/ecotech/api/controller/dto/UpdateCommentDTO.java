package com.ecotech.api.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentDTO(
    @NotBlank(message = "campo obrigatorio")
    @Size(min = 2, max = 500, message = "O conteúdo deve possuir entre 2 e 500 caracteres")
    String content
) {
    
}
