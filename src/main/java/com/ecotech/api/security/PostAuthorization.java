package com.ecotech.api.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ecotech.api.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostAuthorization {

    private final PostRepository postRepository;

    public boolean isOwner(
            UUID postId,
            Authentication authentication
    ) {
        UUID authenticatedUserId =
                UUID.fromString(authentication.getName());

        return postRepository
                .existsByIdAndUserId(
                    postId,
                    authenticatedUserId
                );
    }
}
