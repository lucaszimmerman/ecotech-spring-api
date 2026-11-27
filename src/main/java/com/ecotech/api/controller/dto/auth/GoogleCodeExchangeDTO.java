package com.ecotech.api.controller.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleCodeExchangeDTO(
        @NotBlank(message = "campo obrigatorio")
        String code
) {
}
