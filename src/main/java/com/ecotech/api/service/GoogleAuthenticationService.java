package com.ecotech.api.service;

import java.util.UUID;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ecotech.api.exceptions.OperacaoNaoPermitidaException;
import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.AuthProvider;
import com.ecotech.api.model.enums.UserRole;
import com.ecotech.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationService {

    private final UserRepository userRepository;

    @Transactional
    public User authenticate(OidcUser oidcUser) {

        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        Boolean emailVerified =
                oidcUser.getEmailVerified();

        validateGoogleUser(
                providerId,
                email,
                emailVerified
        );

        return userRepository
                .findByAuthProviderAndProviderId(
                        AuthProvider.GOOGLE,
                        providerId
                )
                .orElseGet(() ->
                        createGoogleUser(
                                providerId,
                                email,
                                name
                        )
                );
    }

    private User createGoogleUser(
            String providerId,
            String email,
            String name
    ) {

        userRepository
                .findByEmailIgnoreCase(email)
                .ifPresent(existingUser -> {

                    throw new OperacaoNaoPermitidaException(
                            "Ja existe uma conta associada a este email."
                    );
                });

        User user = new User();

        user.setEmail(
                email.trim().toLowerCase()
        );

        user.setName(
                StringUtils.hasText(name)
                        ? name.trim()
                        : "Usuario EcoTech"
        );

        user.setUsername(
                generateUniqueUsername(email)
        );

        user.setPassword(null);

        user.setAuthProvider(
                AuthProvider.GOOGLE
        );

        user.setProviderId(providerId);

        user.setEmailVerified(true);

        user.setActive(true);
        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }

    private void validateGoogleUser(
            String providerId,
            String email,
            Boolean emailVerified
    ) {

        if (!StringUtils.hasText(providerId)) {
            throw new OperacaoNaoPermitidaException(
                    "Nao foi possivel identificar a conta Google."
            );
        }

        if (!StringUtils.hasText(email)) {
            throw new OperacaoNaoPermitidaException(
                    "A conta Google nao forneceu um email."
            );
        }

        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new OperacaoNaoPermitidaException(
                    "O email da conta Google nao esta verificado."
            );
        }
    }

    private String generateUniqueUsername(String email) {

        String baseUsername =
                email
                        .substring(0, email.indexOf("@"))
                        .replaceAll("[^a-zA-Z0-9._]", "")
                        .toLowerCase();

        if (baseUsername.length() > 20) {
            baseUsername =
                    baseUsername.substring(0, 20);
        }

        String username = baseUsername;

        while (userRepository.existsByUsernameIgnoreCase(username)) {
            username =
                    baseUsername
                    + "-"
                    + UUID.randomUUID()
                            .toString()
                            .substring(0, 6);
        }

        return username;
    }
}
