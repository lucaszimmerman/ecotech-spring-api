package com.ecotech.api.controller.dto;

public record PostInteractionSummaryDTO(
    long likeCount,
    long commentCount,
    boolean likedByCurrentUser
) {}
