package com.ecotech.api.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.Comment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentValidator {

    public void validateComment(Comment comment) {
        validateRequiredFields(comment);
        validateCommentContent(comment);
    }

    private void validateRequiredFields(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("O comentário não pode ser nulo");
        }

        if (comment.getUser() == null || comment.getUser().getId() == null) {
            throw new CampoInvalidoException("user", "O usuário do comentário é obrigatório");
        }

        if (comment.getPost() == null || comment.getPost().getId() == null) {
            throw new CampoInvalidoException("post", "O Post do comentário é obrigatório");
        }
    }

    private void validateCommentContent(Comment comment) {
        if (!StringUtils.hasText(comment.getContent())) {
            throw new CampoInvalidoException("content", "O Conteúdo do comentário é obrigatório ");
        }
    }

}
