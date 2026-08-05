package com.ecotech.api.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.RegistroDuplicadoException;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.PostLike;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.PostLikeRepository;

@ExtendWith(MockitoExtension.class)
class PostLikeValidatorTest {

    @Mock
    private PostLikeRepository repository;

    private PostLikeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PostLikeValidator(repository);
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

    private PostLike createPostLike() {
        PostLike postLike = new PostLike();

        postLike.setUser(createUser());
        postLike.setPost(createPost());

        return postLike;
    }

    @Test
    void shouldValidatePostLikeSuccessfully() {
        PostLike postLike = createPostLike();

        when(repository.existsByUserIdAndPostId(
                postLike.getUser().getId(),
                postLike.getPost().getId())).thenReturn(false);

        assertThatCode(() -> validator.validateLike(postLike))
                .doesNotThrowAnyException();

        verify(repository).existsByUserIdAndPostId(
                postLike.getUser().getId(),
                postLike.getPost().getId());
    }

    @Test
    void shouldThrowExceptionWhenPostLikeIsNull() {
        assertThatThrownBy(() -> validator.validateLike(null))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("curtida")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("postLike"));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        PostLike postLike = createPostLike();
        postLike.setUser(null);

        assertThatThrownBy(() -> validator.validateLike(postLike))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("usuário")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("user"));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenPostIsNull() {
        PostLike postLike = createPostLike();
        postLike.setPost(null);

        assertThatThrownBy(() -> validator.validateLike(postLike))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("post")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("post"));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyLikedPost() {
        PostLike postLike = createPostLike();

        when(repository.existsByUserIdAndPostId(
                postLike.getUser().getId(),
                postLike.getPost().getId())).thenReturn(true);

        assertThatThrownBy(() -> validator.validateLike(postLike))
                .isInstanceOf(RegistroDuplicadoException.class)
                .hasMessageContaining("curtiu");

        verify(repository).existsByUserIdAndPostId(
                postLike.getUser().getId(),
                postLike.getPost().getId());
    }
}
