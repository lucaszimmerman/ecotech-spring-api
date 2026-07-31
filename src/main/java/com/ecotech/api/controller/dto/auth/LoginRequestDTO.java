package com.ecotech.api.controller.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
    
        @NotBlank(message = "O username é obrigatório.") 
        @Size(max = 30, message = "O username deve possuir no máximo 30 caracteres.")
        String username,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, max = 20, message = "A senha deve possuir entre 6 e 20 caracteres.")
        String password

) {

}
