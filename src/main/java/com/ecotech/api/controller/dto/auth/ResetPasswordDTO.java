package com.ecotech.api.controller.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
        @NotBlank(message = "O token e obrigatorio.")
        String token,

        @NotBlank(message = "A nova senha e obrigatoria.")
        @Size(min = 6, max = 72, message = "A nova senha deve possuir entre 6 e 72 caracteres.")
        String newPassword,

        @NotBlank(message = "A confirmacao da nova senha e obrigatoria.")
        String confirmPassword
) {
}
