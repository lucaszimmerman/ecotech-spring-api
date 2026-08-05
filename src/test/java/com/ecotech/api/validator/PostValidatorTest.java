package com.ecotech.api.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.User;

class PostValidatorTest {

    private PostValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PostValidator();
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

        post.setContent("Conteudo valido do post.");
        post.setUser(createUser());

        return post;
    }

    @Test
    void shouldThrowExceptionWhenPostIsNull() {
        assertThatThrownBy(() -> validator.validatePost(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("post")
                .hasMessageContaining("nulo");
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        Post post = createPost();
        post.setUser(null);

        assertThatThrownBy(() -> validator.validatePost(post))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("post")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("user"));
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoId() {
        Post post = createPost();
        post.getUser().setId(null);

        assertThatThrownBy(() -> validator.validatePost(post))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("post")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("user"));
    }

    @Test
    void shouldThrowExceptionWhenContentIsNull() {
        Post post = createPost();
        post.setContent(null);

        assertThatThrownBy(() -> validator.validatePost(post))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("post")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("content"));
    }

    @Test
    void shouldThrowExceptionWhenContentIsEmpty() {
        Post post = createPost();
        post.setContent("");

        assertThatThrownBy(() -> validator.validatePost(post))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("post")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("content"));
    }

    @Test
    void shouldThrowExceptionWhenContentIsBlank() {
        Post post = createPost();
        post.setContent("   ");

        assertThatThrownBy(() -> validator.validatePost(post))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("post")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("content"));
    }

    @Test
    void shouldValidatePostSuccessfully() {
        Post post = createPost();

        assertThatCode(() -> validator.validatePost(post))
                .doesNotThrowAnyException();
    }
}
