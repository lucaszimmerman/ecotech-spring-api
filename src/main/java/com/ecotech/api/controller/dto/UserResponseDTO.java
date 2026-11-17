package com.ecotech.api.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

import com.ecotech.api.model.enums.UserRole;

public record UserResponseDTO(UUID id,
    String username,
    String email,
    String name,
    @Schema(description = "URL de leitura da foto, ou null. Para S3, expira em 900 segundos por padrao; consulte a API novamente para renovar.")
    String profileImageUrl,
    @Schema(description = "URL de leitura da capa, ou null. Para S3, expira em 900 segundos por padrao; consulte a API novamente para renovar.")
    String coverImageUrl,
    String city,
    String website,
    String bio,
    Boolean active,
    Boolean emailVerified,
    UserRole role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
