package com.ecotech.api.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.Comment;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.User;

class CommentValidatorTest {

    private CommentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CommentValidator();
    }

    private User createUser() {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setUsername("lucas");
        user.setEmail("lucas@email.com");
        user.setPassword("123456");
        user.setName("Lucas Zimmerman");

        return user;
    }

    private Post createPost() {
        Post post = new Post();

        post.setId(UUID.randomUUID());
        post.setContent("Conteudo valido do post.");
        post.setUser(createUser());

        return post;
    }

    private Comment createComment() {
        Comment comment = new Comment();

        comment.setContent("Comentario valido.");
        comment.setUser(createUser());
        comment.setPost(createPost());

        return comment;
    }

    @Test
    void shouldThrowExceptionWhenCommentIsNull() {
        assertThatThrownBy(() -> validator.validateComment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("coment")
                .hasMessageContaining("nulo");
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        Comment comment = createComment();
        comment.setUser(null);

        assertThatThrownBy(() -> validator.validateComment(comment))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("coment")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("user"));
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoId() {
        Comment comment = createComment();
        comment.getUser().setId(null);

        assertThatThrownBy(() -> validator.validateComment(comment))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("coment")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("user"));
    }

    @Test
    void shouldThrowExceptionWhenPostIsNull() {
        Comment comment = createComment();
        comment.setPost(null);

        assertThatThrownBy(() -> validator.validateComment(comment))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("coment")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("post"));
    }

    @Test
    void shouldThrowExceptionWhenPostHasNoId() {
        Comment comment = createComment();
        comment.getPost().setId(null);

        assertThatThrownBy(() -> validator.validateComment(comment))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("coment")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("post"));
    }

    @Test
    void shouldThrowExceptionWhenContentIsNull() {
        Comment comment = createComment();
        comment.setContent(null);

        assertThatThrownBy(() -> validator.validateComment(comment))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("coment")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("content"));
    }

    @Test
    void shouldThrowExceptionWhenContentIsEmpty() {
        Comment comment = createComment();
        comment.setContent("");

        assertThatThrownBy(() -> validator.validateComment(comment))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("coment")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("content"));
    }

    @Test
    void shouldThrowExceptionWhenContentIsBlank() {
        Comment comment = createComment();
        comment.setContent("   ");

        assertThatThrownBy(() -> validator.validateComment(comment))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("coment")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("content"));
    }

    @Test
    void shouldValidateCommentSuccessfully() {
        Comment comment = createComment();

        assertThatCode(() -> validator.validateComment(comment))
                .doesNotThrowAnyException();
    }
}
