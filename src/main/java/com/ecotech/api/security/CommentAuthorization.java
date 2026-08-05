package com.ecotech.api.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ecotech.api.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentAuthorization {

    private final CommentRepository commentRepository;

    public boolean isOwner(
            UUID commentId,
            UUID postId,
            Authentication authentication) {

        UUID authenticatedUserId =
                UUID.fromString(authentication.getName());

        return commentRepository.existsByIdAndPostIdAndUserId(
                commentId,
                postId,
                authenticatedUserId
        );
    }
}
