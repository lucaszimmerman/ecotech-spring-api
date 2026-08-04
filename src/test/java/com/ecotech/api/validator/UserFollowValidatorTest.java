package com.ecotech.api.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
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
import com.ecotech.api.model.User;
import com.ecotech.api.model.UserFollow;
import com.ecotech.api.repository.UserFollowRepository;

@ExtendWith(MockitoExtension.class)
class UserFollowValidatorTest {

    @Mock
    private UserFollowRepository repository;

    private UserFollowValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UserFollowValidator(repository);
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

    private UserFollow createUserFollow() {
        UserFollow userFollow = new UserFollow();

        userFollow.setFollower(createUser());
        userFollow.setFollowed(createUser());

        return userFollow;
    }

    @Test
    void shouldValidateUserFollowSuccessfully() {
        UserFollow userFollow = createUserFollow();

        when(repository.existsByFollowerIdAndFollowedId(
                userFollow.getFollower().getId(),
                userFollow.getFollowed().getId())).thenReturn(false);

        assertThatCode(() -> validator.validateFollowing(userFollow))
                .doesNotThrowAnyException();

        verify(repository).existsByFollowerIdAndFollowedId(
                userFollow.getFollower().getId(),
                userFollow.getFollowed().getId());
    }

    @Test
    void shouldThrowExceptionWhenUserFollowIsNull() {
        assertThatThrownBy(() -> validator.validateFollowing(null))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("relacionamento")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("userFollow"));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenFollowerIsNull() {
        UserFollow userFollow = createUserFollow();
        userFollow.setFollower(null);

        assertThatThrownBy(() -> validator.validateFollowing(userFollow))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("seguidor")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("follower"));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenFollowedIsNull() {
        UserFollow userFollow = createUserFollow();
        userFollow.setFollowed(null);

        assertThatThrownBy(() -> validator.validateFollowing(userFollow))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("seguido")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("followed"));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenUserTriesToFollowYourself() {
        User user = createUser();

        UserFollow userFollow = new UserFollow();
        userFollow.setFollower(user);
        userFollow.setFollowed(user);

        assertThatThrownBy(() -> validator.validateFollowing(userFollow))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("seguir a si mesmo")
                .satisfies(exception -> assertThat(
                        ((CampoInvalidoException) exception).getCampo())
                        .isEqualTo("followed"));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyFollowsAnotherUser() {
        UserFollow userFollow = createUserFollow();

        when(repository.existsByFollowerIdAndFollowedId(
                userFollow.getFollower().getId(),
                userFollow.getFollowed().getId())).thenReturn(true);

        assertThatThrownBy(() -> validator.validateFollowing(userFollow))
                .isInstanceOf(RegistroDuplicadoException.class)
                .hasMessageContaining("seguindo");

        verify(repository).existsByFollowerIdAndFollowedId(
                userFollow.getFollower().getId(),
                userFollow.getFollowed().getId());
    }

    @Test
    void shouldNotValidateDuplicateFollowWhenCannotFollowYourself() {
        User user = createUser();

        UserFollow userFollow = new UserFollow();
        userFollow.setFollower(user);
        userFollow.setFollowed(user);

        assertThatThrownBy(() -> validator.validateFollowing(userFollow))
                .isInstanceOf(CampoInvalidoException.class);

        verify(repository, never()).existsByFollowerIdAndFollowedId(
                user.getId(),
                user.getId());
    }
}
