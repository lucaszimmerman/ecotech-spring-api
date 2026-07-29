package com.ecotech.api.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.ecotech.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository repository;

    private UserValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UserValidator(repository);
    }

    private User createValidUser() {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setUsername("lucas");
        user.setEmail("lucas@email.com");
        user.setPassword("123456");
        user.setName("Lucas Zimmerman");

        return user;
    }

    @Test
    void shouldValidateUserForCreateSuccessfully() {
        User user = createValidUser();

        when(repository.existsByUsernameIgnoreCase(user.getUsername()))
                .thenReturn(false);

        when(repository.existsByEmailIgnoreCase(user.getEmail()))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> validator.validateForCreate(user));

        verify(repository).existsByUsernameIgnoreCase(user.getUsername());
        verify(repository).existsByEmailIgnoreCase(user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExistsOnCreate() {
        User user = createValidUser();

        when(repository.existsByUsernameIgnoreCase(user.getUsername()))
                .thenReturn(true);

        RegistroDuplicadoException exception = assertThrows(
                RegistroDuplicadoException.class,
                () -> validator.validateForCreate(user));

        assertEquals(
                "O nome de usuário já está em uso.",
                exception.getMessage());

        verify(repository).existsByUsernameIgnoreCase(user.getUsername());
        verify(repository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsOnCreate() {
        User user = createValidUser();

        when(repository.existsByUsernameIgnoreCase(user.getUsername()))
                .thenReturn(false);

        when(repository.existsByEmailIgnoreCase(user.getEmail()))
                .thenReturn(true);

        RegistroDuplicadoException exception = assertThrows(
                RegistroDuplicadoException.class,
                () -> validator.validateForCreate(user));

        assertEquals(
                "O e-mail já está em uso.",
                exception.getMessage());

        verify(repository).existsByUsernameIgnoreCase(user.getUsername());
        verify(repository).existsByEmailIgnoreCase(user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUsernameContainsWhitespace() {
        User user = createValidUser();
        user.setUsername("lucas zimmerman");

        CampoInvalidoException exception = assertThrows(
                CampoInvalidoException.class,
                () -> validator.validateForCreate(user));

        assertEquals("username", exception.getCampo());

        assertEquals(
                "O nome de usuário não pode conter espaços.",
                exception.getMessage());

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsBlank() {
        User user = createValidUser();
        user.setUsername("   ");

        CampoInvalidoException exception = assertThrows(
                CampoInvalidoException.class,
                () -> validator.validateForCreate(user));

        assertEquals("username", exception.getCampo());

        assertEquals(
                "O nome de usuário é obrigatório.",
                exception.getMessage());

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsNull() {
        User user = createValidUser();
        user.setUsername(null);

        CampoInvalidoException exception = assertThrows(
                CampoInvalidoException.class,
                () -> validator.validateForCreate(user));

        assertEquals("username", exception.getCampo());

        assertEquals(
                "O nome de usuário é obrigatório.",
                exception.getMessage());

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = createValidUser();
        user.setEmail("   ");

        when(repository.existsByUsernameIgnoreCase(user.getUsername()))
                .thenReturn(false);

        CampoInvalidoException exception = assertThrows(
                CampoInvalidoException.class,
                () -> validator.validateForCreate(user));

        assertEquals("email", exception.getCampo());

        assertEquals(
                "O e-mail é obrigatório.",
                exception.getMessage());

        verify(repository).existsByUsernameIgnoreCase(user.getUsername());
        verify(repository, never()).existsByEmailIgnoreCase(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNullOnCreate() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateForCreate(null));

        assertEquals(
                "O usuário informado não pode ser nulo.",
                exception.getMessage());

        verifyNoInteractions(repository);
    }

    @Test
    void shouldValidateUserForUpdateSuccessfully() {
        User user = createValidUser();

        when(repository.existsByUsernameIgnoreCaseAndIdNot(
                user.getUsername(),
                user.getId())).thenReturn(false);

        assertDoesNotThrow(
                () -> validator.validateForUpdate(user));

        verify(repository).existsByUsernameIgnoreCaseAndIdNot(
                user.getUsername(),
                user.getId());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExistsOnUpdate() {
        User user = createValidUser();

        when(repository.existsByUsernameIgnoreCaseAndIdNot(
                user.getUsername(),
                user.getId())).thenReturn(true);

        RegistroDuplicadoException exception = assertThrows(
                RegistroDuplicadoException.class,
                () -> validator.validateForUpdate(user));

        assertEquals(
                "O nome de usuário já está em uso.",
                exception.getMessage());

        verify(repository).existsByUsernameIgnoreCaseAndIdNot(
                user.getUsername(),
                user.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoIdOnUpdate() {
        User user = createValidUser();
        user.setId(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateForUpdate(user));

        assertEquals(
                "Para atualizar, o usuário precisa possuir um ID.",
                exception.getMessage());

        verifyNoInteractions(repository);
    }
}
