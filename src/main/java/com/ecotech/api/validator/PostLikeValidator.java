package com.ecotech.api.validator;

import org.springframework.stereotype.Component;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.RegistroDuplicadoException;
import com.ecotech.api.model.PostLike;
import com.ecotech.api.repository.PostLikeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostLikeValidator {

    private final PostLikeRepository repository;

    public void validateLike(PostLike postLike) {
        validateRequiredFields(postLike);
        validateDuplicateLike(postLike);
    }

    private void validateRequiredFields(PostLike postLike) {
        if (postLike == null) {
            throw new CampoInvalidoException(
                    "postLike",
                    "A curtida do post é obrigatória.");
        }

        if (postLike.getUser() == null || postLike.getUser().getId() == null) {
            throw new CampoInvalidoException(
                    "user",
                    "O usuário da curtida é obrigatório.");
        }

        if (postLike.getPost() == null || postLike.getPost().getId() == null) {
            throw new CampoInvalidoException(
                    "post",
                    "O post da curtida é obrigatório.");
        }
    }

    private void validateDuplicateLike(PostLike postLike) {
        boolean alreadyLiked = repository.existsByUserIdAndPostId(
                postLike.getUser().getId(),
                postLike.getPost().getId());

        if (alreadyLiked) {
            throw new RegistroDuplicadoException(
                    "O usuário já curtiu este post.");
        }
    }
}
