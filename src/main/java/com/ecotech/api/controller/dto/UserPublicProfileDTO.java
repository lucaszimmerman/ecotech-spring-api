package com.ecotech.api.controller.dto;

import java.util.UUID;

public record UserPublicProfileDTO(
    UUID id,
    String username,
    String name,
    String profileImageUrl,
    String coverImageUrl,
    String city,
    String website,
    String bio
) {}
