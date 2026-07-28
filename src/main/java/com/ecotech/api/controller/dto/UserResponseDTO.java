package com.ecotech.api.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ecotech.api.model.enums.UserRole;

public record UserResponseDTO(UUID id,
    String username,
    String email,
    String name,
    String profileImageUrl,
    String coverImageUrl,
    String city,
    String website,
    String bio,
    Boolean active,
    UserRole role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
