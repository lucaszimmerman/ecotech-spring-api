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
import com.ecotech.api.model.Comment;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.CommentRepository;
import com.ecotech.api.repository.PostRepository;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.CommentValidator;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final String USER_NOT_FOUND_MESSAGE = "encontrado";
    private static final String POST_NOT_FOUND_MESSAGE = "encontrado";
    private static final String COMMENT_NOT_FOUND_MESSAGE = "Coment";

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentValidator validator;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService service;

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

    private Comment createComment(User user, Post post) {
        Comment comment = new Comment();

        comment.setId(UUID.randomUUID());
        comment.setContent("Comentario valido.");
        comment.setUser(user);
        comment.setPost(post);

        return comment;
    }

    @Test
    void shouldCreateCommentAssociatingInformedUserAndPostSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        Comment comment = new Comment();
        comment.setContent("Comentario valido.");

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(commentRepository.save(comment))
                .thenReturn(comment);

        Comment result = service.save(comment, user.getId(), post.getId());

        assertThat(result).isSameAs(comment);

        ArgumentCaptor<Comment> commentCaptor =
                ArgumentCaptor.forClass(Comment.class);

        verify(validator).validateComment(commentCaptor.capture());
        verify(commentRepository).save(commentCaptor.getValue());
        verify(userRepository).findById(user.getId());
        verify(postRepository).findById(post.getId());

        assertThat(commentCaptor.getValue().getUser()).isEqualTo(user);
        assertThat(commentCaptor.getValue().getPost()).isEqualTo(post);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistOnCreate() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Comment comment = new Comment();
        comment.setContent("Comentario valido.");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(comment, userId, postId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(USER_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(userId);
        verify(postRepository, never()).findById(postId);
        verifyNoInteractions(validator);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenPostDoesNotExistOnCreate() {
        User user = createUser("lucas");
        UUID postId = UUID.randomUUID();
        Comment comment = new Comment();
        comment.setContent("Comentario valido.");

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        when(postRepository.findById(postId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(comment, user.getId(), postId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(POST_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(user.getId());
        verify(postRepository).findById(postId);
        verifyNoInteractions(validator);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldFindCommentByIdSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        Comment comment = createComment(user, post);

        when(commentRepository.findById(comment.getId()))
                .thenReturn(Optional.of(comment));

        Comment result = service.findById(comment.getId());

        assertThat(result).isEqualTo(comment);

        verify(commentRepository).findById(comment.getId());
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldThrowExceptionWhenCommentDoesNotExistOnFindById() {
        UUID commentId = UUID.randomUUID();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(commentId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(COMMENT_NOT_FOUND_MESSAGE)
                .hasMessageContaining("encontrado");

        verify(commentRepository).findById(commentId);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldFindCommentByIdAndPostIdSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        Comment comment = createComment(user, post);

        when(commentRepository.findByIdAndPostId(comment.getId(), post.getId()))
                .thenReturn(Optional.of(comment));

        Comment result = service.findByIdAndPostId(comment.getId(), post.getId());

        assertThat(result).isEqualTo(comment);

        verify(commentRepository).findByIdAndPostId(
                comment.getId(),
                post.getId());
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldThrowExceptionWhenCommentDoesNotExistOnFindByIdAndPostId() {
        UUID commentId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(commentRepository.findByIdAndPostId(commentId, postId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByIdAndPostId(commentId, postId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(COMMENT_NOT_FOUND_MESSAGE)
                .hasMessageContaining("encontrado");

        verify(commentRepository).findByIdAndPostId(commentId, postId);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldUpdateValidCommentSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        Comment comment = createComment(user, post);

        when(commentRepository.save(comment))
                .thenReturn(comment);

        Comment result = service.update(comment);

        assertThat(result).isSameAs(comment);

        verify(validator).validateComment(comment);
        verify(commentRepository).save(comment);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldDeleteCommentSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        Comment comment = createComment(user, post);

        service.delete(comment);

        verify(commentRepository).delete(comment);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(postRepository);
    }

    @Test
    void shouldFindCommentsByPostIdWithPageableSuccessfully() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> expectedPage = new PageImpl<>(
                List.of(createComment(user, post)),
                pageable,
                1);

        when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(commentRepository.findByPostId(post.getId(), pageable))
                .thenReturn(expectedPage);

        Page<Comment> result = service.findByPostId(post.getId(), pageable);

        assertThat(result).isSameAs(expectedPage);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(postRepository).findById(post.getId());
        verify(commentRepository).findByPostId(post.getId(), pageable);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
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
        verify(commentRepository, never()).findByPostId(postId, pageable);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldCountCommentsByPostIdSuccessfully() {
        Post post = createPost(createUser("maria"));

        when(postRepository.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(commentRepository.countByPostId(post.getId()))
                .thenReturn(3L);

        long result = service.countByPostId(post.getId());

        assertThat(result).isEqualTo(3L);

        verify(postRepository).findById(post.getId());
        verify(commentRepository).countByPostId(post.getId());
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenPostDoesNotExistOnCountByPostId() {
        UUID postId = UUID.randomUUID();

        when(postRepository.findById(postId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.countByPostId(postId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(POST_NOT_FOUND_MESSAGE);

        verify(postRepository).findById(postId);
        verify(commentRepository, never()).countByPostId(postId);
        verifyNoInteractions(validator);
        verifyNoInteractions(userRepository);
    }
}
