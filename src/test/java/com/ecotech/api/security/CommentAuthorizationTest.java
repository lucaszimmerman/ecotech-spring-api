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

import com.ecotech.api.repository.CommentRepository;

@ExtendWith(MockitoExtension.class)
class CommentAuthorizationTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentAuthorization commentAuthorization;

    @Test
    void shouldReturnTrueWhenAuthenticatedUserIsCommentOwner() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(authentication.getName())
                .thenReturn(userId.toString());

        when(commentRepository.existsByIdAndPostIdAndUserId(
                commentId,
                postId,
                userId)).thenReturn(true);

        boolean result = commentAuthorization.isOwner(
                commentId,
                postId,
                authentication);

        assertThat(result).isTrue();

        verify(authentication).getName();
        verify(commentRepository).existsByIdAndPostIdAndUserId(
                commentId,
                postId,
                userId);
    }

    @Test
    void shouldReturnFalseWhenAuthenticatedUserIsNotCommentOwner() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(authentication.getName())
                .thenReturn(userId.toString());

        when(commentRepository.existsByIdAndPostIdAndUserId(
                commentId,
                postId,
                userId)).thenReturn(false);

        boolean result = commentAuthorization.isOwner(
                commentId,
                postId,
                authentication);

        assertThat(result).isFalse();

        verify(authentication).getName();
        verify(commentRepository).existsByIdAndPostIdAndUserId(
                commentId,
                postId,
                userId);
    }
}
