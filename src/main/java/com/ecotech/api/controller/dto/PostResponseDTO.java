package com.ecotech.api.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponseDTO(
    UUID id,
    String content,
    String imageUrl,
    UserSummaryDTO user,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
}
