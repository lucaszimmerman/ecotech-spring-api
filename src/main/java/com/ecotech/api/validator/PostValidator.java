package com.ecotech.api.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.Post;

@Component
public class PostValidator {

    public void validatePost(Post post) {
        validatePostNotNull(post);
        validatePostOwner(post);
        validatePostContent(post);
    }

    private void validatePostNotNull(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("O post não pode ser nulo.");
        }
    }

    private void validatePostOwner(Post post) {
        if (post.getUser() == null || post.getUser().getId() == null) {
            throw new CampoInvalidoException(
                    "user",
                    "O usuário do post é obrigatório.");
        }
    }

    private void validatePostContent(Post post) {
        if (!StringUtils.hasText(post.getContent())) {
            throw new CampoInvalidoException(
                    "content",
                    "O conteúdo do post é obrigatório.");
        }
    }
}
