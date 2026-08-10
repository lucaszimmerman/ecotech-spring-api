package com.ecotech.api.controller.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordDTO(
        @NotBlank(message = "campo obrigatorio")
        @Email
        String email
) {
}
