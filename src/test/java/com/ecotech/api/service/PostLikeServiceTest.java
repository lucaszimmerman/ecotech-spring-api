package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
import com.ecotech.api.model.PostLike;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.PostLikeRepository;
import com.ecotech.api.repository.PostRepository;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.PostLikeValidator;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    private static final String USER_NOT_FOUND_MESSAGE = "encontrado";
    private static final String POST_NOT_FOUND_MESSAGE = "encontrado";

    @Mock
    private PostLikeRepository repository;

    @Mock
    private PostLikeValidator validator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostLikeService service;

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

    private PostLike createPostLike(User user, Post post) {
        PostLike postLike = new PostLike();

        postLike.setId(UUID.randomUUID());
        postLike.setUser(user);
        postLike.setPost(post);

        return postLike;
    }

    @Test
    void shouldLikePostSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        service.like(user.getId(), post.getId());

        ArgumentCaptor<PostLike> postLikeCaptor =
                ArgumentCaptor.forClass(PostLike.class);

        verify(validator).validateLike(postLikeCaptor.capture());
        verify(repository).save(postLikeCaptor.getValue());

        PostLike savedPostLike = postLikeCaptor.getValue();

        assertThat(savedPostLike.getUser()).isEqualTo(user);
        assertThat(savedPostLike.getPost()).isEqualTo(post);

        verify(userRepository).findById(user.getId());
        verify(postRepository).findById(post.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistOnLike() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.like(userId, postId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(USER_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(userId);
        verify(postRepository, never()).findById(postId);
        verifyNoInteractions(validator);
        verify(repository, never()).save(any(PostLike.class));
    }

    @Test
    void shouldThrowExceptionWhenPostDoesNotExistOnLike() {
        User user = createUser("lucas");
        UUID postId = UUID.randomUUID();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(postRepository.findById(postId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.like(user.getId(), postId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(POST_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(user.getId());
        verify(postRepository).findById(postId);
        verifyNoInteractions(validator);
        verify(repository, never()).save(any(PostLike.class));
    }

    @Test
    void shouldUnlikePostSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        PostLike postLike = createPostLike(user, post);

        when(repository.findByUserIdAndPostId(user.getId(), post.getId()))
                .thenReturn(Optional.of(postLike));

        service.unlike(user.getId(), post.getId());

        verify(repository).findByUserIdAndPostId(user.getId(), post.getId());
        verify(repository).delete(postLike);
    }

    @Test
    void shouldDoNothingWhenPostLikeDoesNotExistOnUnlike() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(repository.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.empty());

        assertThatCode(() -> service.unlike(userId, postId))
                .doesNotThrowAnyException();

        verify(repository).findByUserIdAndPostId(userId, postId);
        verify(repository, never()).delete(any(PostLike.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(postRepository);
        verifyNoInteractions(validator);
    }

    @Test
    void shouldFindPostLikesByPostIdSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        Pageable pageable = PageRequest.of(0, 10);
        Page<PostLike> expectedPage = new PageImpl<>(
                List.of(createPostLike(user, post)),
                pageable,
                1);

        when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(repository.findByPostId(post.getId(), pageable))
                .thenReturn(expectedPage);

        Page<PostLike> result = service.findByPostId(post.getId(), pageable);

        assertThat(result).isSameAs(expectedPage);
        assertThat(result.getContent())
                .hasSize(1)
                .extracting(PostLike::getUser)
                .containsExactly(user);

        verify(postRepository).findById(post.getId());
        verify(repository).findByPostId(post.getId(), pageable);
    }

    @Test
    void shouldCountPostLikesSuccessfully() {
        Post post = createPost(createUser("maria"));

        when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(repository.countByPostId(post.getId()))
                .thenReturn(3L);

        long result = service.countByPostId(post.getId());

        assertThat(result).isEqualTo(3L);

        verify(postRepository).findById(post.getId());
        verify(repository).countByPostId(post.getId());
    }

    @Test
    void shouldReturnTrueWhenUserLikedPost() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(repository.existsByUserIdAndPostId(user.getId(), post.getId()))
                .thenReturn(true);

        boolean result = service.likedByUser(user.getId(), post.getId());

        assertThat(result).isTrue();

        verify(userRepository).findById(user.getId());
        verify(postRepository).findById(post.getId());
        verify(repository).existsByUserIdAndPostId(user.getId(), post.getId());
    }

    @Test
    void shouldThrowExceptionWhenPostDoesNotExistOnFindByPostId() {
        UUID postId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(postRepository.findById(postId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByPostId(postId, pageable))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(POST_NOT_FOUND_MESSAGE);

        verify(postRepository).findById(postId);
        verify(repository, never()).findByPostId(postId, pageable);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistOnLikedByUser() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.likedByUser(userId, postId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(USER_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(userId);
        verify(postRepository, never()).findById(postId);
        verify(repository, never()).existsByUserIdAndPostId(userId, postId);
    }
}
