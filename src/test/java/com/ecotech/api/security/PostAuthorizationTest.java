package com.ecotech.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.ecotech.api.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostAuthorizationTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostAuthorization postAuthorization;

    @Test
    void shouldReturnTrueWhenAuthenticatedUserIsPostOwner() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(authentication.getName())
                .thenReturn(userId.toString());

        when(postRepository.existsByIdAndUserId(postId, userId))
                .thenReturn(true);

        boolean result = postAuthorization.isOwner(postId, authentication);

        assertThat(result).isTrue();

        verify(authentication).getName();
        verify(postRepository).existsByIdAndUserId(postId, userId);
    }

    @Test
    void shouldReturnFalseWhenAuthenticatedUserIsNotPostOwner() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(authentication.getName())
                .thenReturn(userId.toString());

        when(postRepository.existsByIdAndUserId(postId, userId))
                .thenReturn(false);

        boolean result = postAuthorization.isOwner(postId, authentication);

        assertThat(result).isFalse();

        verify(authentication).getName();
        verify(postRepository).existsByIdAndUserId(postId, userId);
    }
}
