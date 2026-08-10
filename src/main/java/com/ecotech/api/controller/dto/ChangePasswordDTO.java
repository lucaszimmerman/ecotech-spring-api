package com.ecotech.api.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(

        @NotBlank(message = "A senha atual é obrigatória.")
        String currentPassword,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 6, max = 72, message = "A nova senha deve possuir entre 6 e 72 caracteres.")
        String newPassword,

        @NotBlank(message = "A confirmação da nova senha é obrigatória.")
        String confirmPassword
) {
}
