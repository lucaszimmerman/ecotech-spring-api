package com.ecotech.api.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public record PostResponseDTO(
    UUID id,
    String content,
    @Schema(description = "URL de leitura da imagem, ou null. Para S3, expira em 900 segundos por padrao; consulte a API novamente para renovar.")
    String imageUrl,
    UserSummaryDTO user,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    long likeCount,
    long commentCount,
    boolean likedByCurrentUser
) {
    
}
