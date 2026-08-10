package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecotech.api.controller.dto.ChangePasswordDTO;
import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.UserValidator;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserValidator validator;

    @Mock
    private ImageStorageService imageStorageService;

    private PasswordEncoder encoder;

    private UserService service;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder(10);
        service = new UserService(
                repository,
                validator,
                encoder,
                imageStorageService);
    }

    private User createUser(UUID userId, String rawPassword) {
        User user = new User();

        user.setId(userId);
        user.setUsername("lucas");
        user.setEmail("lucas@email.com");
        user.setPassword(encoder.encode(rawPassword));
        user.setName("Lucas Zimmerman");

        return user;
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, "old-password");
        ChangePasswordDTO dto = new ChangePasswordDTO(
                "old-password",
                "new-password",
                "new-password");

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        service.changePassword(userId, dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(repository).findById(userId);
        verify(repository).save(userCaptor.capture());
        verifyNoInteractions(validator, imageStorageService);

        String storedPassword = userCaptor.getValue().getPassword();

        assertThat(storedPassword).isNotEqualTo("new-password");
        assertThat(encoder.matches("new-password", storedPassword)).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, "old-password");
        ChangePasswordDTO dto = new ChangePasswordDTO(
                "wrong-password",
                "new-password",
                "new-password");

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changePassword(userId, dto))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("incorreta");

        verify(repository).findById(userId);
        verify(repository, never()).save(user);
        verifyNoInteractions(validator, imageStorageService);
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordAndConfirmationAreDifferent() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, "old-password");
        ChangePasswordDTO dto = new ChangePasswordDTO(
                "old-password",
                "new-password",
                "other-password");

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changePassword(userId, dto))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("não confere");

        verify(repository).findById(userId);
        verify(repository, never()).save(user);
        verifyNoInteractions(validator, imageStorageService);
    }

    @Test
    void shouldThrowExceptionWhenNewPasswordIsEqualToCurrentPassword() {
        UUID userId = UUID.randomUUID();
        User user = createUser(userId, "same-password");
        ChangePasswordDTO dto = new ChangePasswordDTO(
                "same-password",
                "same-password",
                "same-password");

        when(repository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changePassword(userId, dto))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("diferente");

        verify(repository).findById(userId);
        verify(repository, never()).save(user);
        verifyNoInteractions(validator, imageStorageService);
    }
}
