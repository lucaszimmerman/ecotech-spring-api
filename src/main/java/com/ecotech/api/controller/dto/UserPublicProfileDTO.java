package com.ecotech.api.controller.dto;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserPublicProfileDTO(
    UUID id,
    String username,
    String name,
    @Schema(description = "URL de leitura da foto, ou null. Para S3, expira em 900 segundos por padrao; consulte a API novamente para renovar.")
    String profileImageUrl,
    @Schema(description = "URL de leitura da capa, ou null. Para S3, expira em 900 segundos por padrao; consulte a API novamente para renovar.")
    String coverImageUrl,
    String city,
    String website,
    String bio
) {}
