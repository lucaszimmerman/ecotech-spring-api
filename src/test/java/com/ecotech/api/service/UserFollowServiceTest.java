package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.User;
import com.ecotech.api.model.UserFollow;
import com.ecotech.api.repository.UserFollowRepository;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.UserFollowValidator;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceTest {

    private static final String USER_NOT_FOUND_MESSAGE = "encontrado";
    private static final String CANNOT_FOLLOW_YOURSELF_MESSAGE =
            "O usuario nao pode seguir a si mesmo.";

    @Mock
    private UserFollowRepository repository;

    @Mock
    private UserFollowValidator validator;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFollowService service;

    private User createUser(String username) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(username + "@email.com");
        user.setPassword("123456");
        user.setName(username);

        return user;
    }

    private UserFollow createUserFollow(User follower, User followed) {
        UserFollow userFollow = new UserFollow();

        userFollow.setId(UUID.randomUUID());
        userFollow.setFollower(follower);
        userFollow.setFollowed(followed);

        return userFollow;
    }

    @Test
    void shouldFollowAnotherUserSuccessfully() {
        User follower = createUser("lucas");
        User followed = createUser("maria");

        when(userRepository.findById(follower.getId()))
                .thenReturn(Optional.of(follower));

        when(userRepository.findById(followed.getId()))
                .thenReturn(Optional.of(followed));

        service.follow(follower.getId(), followed.getId());

        ArgumentCaptor<UserFollow> userFollowCaptor =
                ArgumentCaptor.forClass(UserFollow.class);

        verify(validator).validateFollowing(userFollowCaptor.capture());
        verify(repository).save(userFollowCaptor.getValue());

        UserFollow savedUserFollow = userFollowCaptor.getValue();

        assertThat(savedUserFollow.getFollower()).isEqualTo(follower);
        assertThat(savedUserFollow.getFollowed()).isEqualTo(followed);

        verify(userRepository).findById(follower.getId());
        verify(userRepository).findById(followed.getId());
    }

    @Test
    void shouldThrowExceptionWhenFollowerDoesNotExistOnFollow() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        when(userRepository.findById(followerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.follow(followerId, followedId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(USER_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(followerId);
        verify(userRepository, never()).findById(followedId);
        verifyNoInteractions(validator);
        verify(repository, never()).save(any(UserFollow.class));
    }

    @Test
    void shouldThrowExceptionWhenFollowedDoesNotExistOnFollow() {
        User follower = createUser("lucas");
        UUID followedId = UUID.randomUUID();

        when(userRepository.findById(follower.getId()))
                .thenReturn(Optional.of(follower));

        when(userRepository.findById(followedId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.follow(follower.getId(), followedId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(USER_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(follower.getId());
        verify(userRepository).findById(followedId);
        verifyNoInteractions(validator);
        verify(repository, never()).save(any(UserFollow.class));
    }

    @Test
    void shouldPropagateValidatorExceptionWhenUserTriesToFollowYourself() {
        User user = createUser("lucas");

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        CampoInvalidoException exception = new CampoInvalidoException(
                "followed",
                CANNOT_FOLLOW_YOURSELF_MESSAGE);

        org.mockito.Mockito.doThrow(exception)
                .when(validator)
                .validateFollowing(any(UserFollow.class));

        assertThatThrownBy(() -> service.follow(user.getId(), user.getId()))
                .isSameAs(exception);

        verify(userRepository, times(2)).findById(user.getId());
        verify(validator).validateFollowing(any(UserFollow.class));
        verify(repository, never()).save(any(UserFollow.class));
    }

    @Test
    void shouldUnfollowAnotherUserSuccessfully() {
        User follower = createUser("lucas");
        User followed = createUser("maria");
        UserFollow userFollow = createUserFollow(follower, followed);

        when(repository.findByFollowerIdAndFollowedId(
                follower.getId(),
                followed.getId())).thenReturn(Optional.of(userFollow));

        service.unfollow(follower.getId(), followed.getId());

        verify(repository).findByFollowerIdAndFollowedId(
                follower.getId(),
                followed.getId());

        verify(repository).delete(userFollow);
    }

    @Test
    void shouldDoNothingWhenUserFollowDoesNotExistOnUnfollow() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        when(repository.findByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(Optional.empty());

        assertThatCode(() -> service.unfollow(followerId, followedId))
                .doesNotThrowAnyException();

        verify(repository).findByFollowerIdAndFollowedId(
                followerId,
                followedId);

        verify(repository, never()).delete(any(UserFollow.class));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(validator);
    }

    @Test
    void shouldFindFollowersSuccessfully() {
        User followed = createUser("maria");
        User follower = createUser("lucas");
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserFollow> expectedPage = new PageImpl<>(
                List.of(createUserFollow(follower, followed)),
                pageable,
                1);

        when(userRepository.findById(followed.getId()))
                .thenReturn(Optional.of(followed));

        when(repository.findByFollowedId(followed.getId(), pageable))
                .thenReturn(expectedPage);

        Page<UserFollow> result = service.findFollowers(
                followed.getId(),
                pageable);

        assertThat(result).isSameAs(expectedPage);
        assertThat(result.getContent())
                .hasSize(1)
                .extracting(UserFollow::getFollower)
                .containsExactly(follower);

        verify(userRepository).findById(followed.getId());
        verify(repository).findByFollowedId(followed.getId(), pageable);
    }

    @Test
    void shouldFindFollowingSuccessfully() {
        User follower = createUser("lucas");
        User followed = createUser("maria");
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserFollow> expectedPage = new PageImpl<>(
                List.of(createUserFollow(follower, followed)),
                pageable,
                1);

        when(userRepository.findById(follower.getId()))
                .thenReturn(Optional.of(follower));

        when(repository.findByFollowerId(follower.getId(), pageable))
                .thenReturn(expectedPage);

        Page<UserFollow> result = service.findFollowing(
                follower.getId(),
                pageable);

        assertThat(result).isSameAs(expectedPage);
        assertThat(result.getContent())
                .hasSize(1)
                .extracting(UserFollow::getFollowed)
                .containsExactly(followed);

        verify(userRepository).findById(follower.getId());
        verify(repository).findByFollowerId(follower.getId(), pageable);
    }

    @Test
    void shouldKeepPaginationWhenFindingFollowers() {
        User followed = createUser("maria");
        Pageable pageable = PageRequest.of(1, 2);
        Page<UserFollow> expectedPage = new PageImpl<>(
                List.of(),
                pageable,
                3);

        when(userRepository.findById(followed.getId()))
                .thenReturn(Optional.of(followed));

        when(repository.findByFollowedId(followed.getId(), pageable))
                .thenReturn(expectedPage);

        Page<UserFollow> result = service.findFollowers(
                followed.getId(),
                pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);

        verify(repository).findByFollowedId(followed.getId(), pageable);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistOnFindFollowers() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findFollowers(userId, pageable))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(USER_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(userId);
        verify(repository, never()).findByFollowedId(userId, pageable);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistOnFindFollowing() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findFollowing(userId, pageable))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining(USER_NOT_FOUND_MESSAGE);

        verify(userRepository).findById(userId);
        verify(repository, never()).findByFollowerId(userId, pageable);
    }
}
