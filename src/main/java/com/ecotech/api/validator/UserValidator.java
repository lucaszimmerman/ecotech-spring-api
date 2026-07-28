package com.ecotech.api.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.RegistroDuplicadoException;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository repository;

   public void validateForCreate(User user) {
        validateUserNotNull(user);
        validateUsernameFormat(user.getUsername());
        validateUsernameForCreate(user.getUsername());
        validateEmailForCreate(user.getEmail());
    }

    public void validateForUpdate(User user) {
        validateUserNotNull(user);
        validateUserId(user);
        validateUsernameFormat(user.getUsername());
        validateUsernameForUpdate(user);
    }

    private void validateUserNotNull(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                "O usuário informado não pode ser nulo."
            );
        }
    }

    private void validateUserId(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException(
                "Para atualizar, o usuário precisa possuir um ID."
            );
        }
    }

    private void validateUsernameFormat(String username) {
        if (!StringUtils.hasText(username)) {
            throw new CampoInvalidoException(
                "username",
                "O nome de usuário é obrigatório."
            );
        }

        if (username.chars().anyMatch(Character::isWhitespace)) {
            throw new CampoInvalidoException(
                "username",
                "O nome de usuário não pode conter espaços."
            );
        }
    }

    private void validateUsernameForCreate(String username) {
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new RegistroDuplicadoException(
                "O nome de usuário já está em uso."
            );
        }
    }

    private void validateUsernameForUpdate(User user) {
        boolean usernameAlreadyExists =
            repository.existsByUsernameIgnoreCaseAndIdNot(
                user.getUsername(),
                user.getId()
            );

        if (usernameAlreadyExists) {
            throw new RegistroDuplicadoException(
                "O nome de usuário já está em uso."
            );
        }
    }

    private void validateEmailForCreate(String email) {
        if (!StringUtils.hasText(email)) {
            throw new CampoInvalidoException(
                "email",
                "O e-mail é obrigatório."
            );
        }

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new RegistroDuplicadoException(
                "O e-mail já está em uso."
            );
        }
    }
}
