package com.ecotech.api.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponseDTO(
    UUID id,
    String content,
    UserSummaryDTO user,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
}
