package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.PostRepository;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.PostValidator;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private static final String USER_NOT_FOUND_MESSAGE = "encontrado";
    private static final String POST_NOT_FOUND_MESSAGE = "Post";

    @Mock
    private PostRepository repository;

    @Mock
    private PostValidator validator;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService service;

    private User createUser(String username) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(username + "@email.com");
        user.setPassword("123456");
        user.setName(username);

        return user;
    }

    private Post createPost(User user) {
        Post post = new Post();

        post.setId(UUID.randomUUID());
        post.setContent("Conteudo valido do post.");
        post.setUser(user);

        return post;
    }

    @Test
    void shouldCreatePostAssociatingInformedUserSuccessfully() {
        User user = createUser("lucas");
        Post post = new Post();
        post.setContent("Conteudo valido do post.");

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(repository.save(post))
                .thenReturn(post);

        Post result = service.save(post, user.getId());

        assertThat(result).isSameAs(post);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        verify(validator).validatePost(postCaptor.capture());
        verify(repository).save(postCaptor.getValue());
        verify(userRepository).findById(user.getId());

        assertThat(postCaptor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistOnCreate() {
        UUID userId = UUID.randomUUID();
        Post post = new Post();
        post.setContent("Conteudo valido do post.");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(post, userId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(USER_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(userId);
        verifyNoInteractions(validator);
        verify(repository, never()).save(any(Post.class));
    }

    @Test
    void shouldUpdateValidPostSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(user);

        when(repository.save(post))
                .thenReturn(post);

        Post result = service.update(post);

        assertThat(result).isSameAs(post);

        verify(validator).validatePost(post);
        verify(repository).save(post);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldFindPostByIdSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(user);

        when(repository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        Post result = service.findById(post.getId());

        assertThat(result).isEqualTo(post);

        verify(repository).findById(post.getId());
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenPostDoesNotExistOnFindById() {
        UUID postId = UUID.randomUUID();

        when(repository.findById(postId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(postId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(POST_NOT_FOUND_MESSAGE)
                .hasMessageContaining("encontrado");

        verify(repository).findById(postId);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldFindAllPostsWithPageableSuccessfully() {
        User user = createUser("lucas");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> expectedPage = new PageImpl<>(
                List.of(createPost(user)),
                pageable,
                1);

        when(repository.findAll(pageable))
                .thenReturn(expectedPage);

        Page<Post> result = service.findAll(pageable);

        assertThat(result).isSameAs(expectedPage);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(repository).findAll(pageable);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldFindPostsByUserIdWithPageableSuccessfully() {
        User user = createUser("lucas");
        Pageable pageable = PageRequest.of(1, 2);
        Page<Post> expectedPage = new PageImpl<>(
                List.of(createPost(user)),
                pageable,
                3);

        when(repository.findByUserId(user.getId(), pageable))
                .thenReturn(expectedPage);

        Page<Post> result = service.findByUserId(user.getId(), pageable);

        assertThat(result).isSameAs(expectedPage);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);

        verify(repository).findByUserId(user.getId(), pageable);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldDeletePostSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(user);

        service.delete(post);

        verify(repository).delete(post);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
    }
}
