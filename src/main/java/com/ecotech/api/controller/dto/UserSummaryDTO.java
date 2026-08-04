package com.ecotech.api.controller.dto;

import java.util.UUID;

public record UserSummaryDTO(
    UUID id,
    String username,
    String name,
    String profileImageUrl
) {
    
}
