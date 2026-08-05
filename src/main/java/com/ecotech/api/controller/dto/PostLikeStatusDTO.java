package com.ecotech.api.controller.dto;

public record PostLikeStatusDTO(
        long likeCount,
        boolean likedByCurrentUser
) {

}
