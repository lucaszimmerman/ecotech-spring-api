package com.ecotech.api.controller.dto.auth;

import java.util.UUID;

import com.ecotech.api.model.enums.UserRole;

public record LoginResponseDTO(

        UUID id,
        String username,
        String name,
        UserRole role
    ) {

}
