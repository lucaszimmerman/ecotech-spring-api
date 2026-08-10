package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.ecotech.api.exceptions.OperacaoNaoPermitidaException;
import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.AuthProvider;
import com.ecotech.api.model.enums.UserRole;
import com.ecotech.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GoogleAuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OidcUser oidcUser;

    @InjectMocks
    private GoogleAuthenticationService service;

    @Test
    void shouldReturnExistingGoogleUser() {
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());

        when(oidcUser.getSubject()).thenReturn("google-subject");
        when(oidcUser.getEmail()).thenReturn("lucas@email.com");
        when(oidcUser.getFullName()).thenReturn("Lucas Zimmerman");
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(userRepository.findByAuthProviderAndProviderId(
                AuthProvider.GOOGLE,
                "google-subject"))
                .thenReturn(Optional.of(existingUser));

        User user = service.authenticate(oidcUser);

        assertThat(user).isSameAs(existingUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldCreateGoogleUserWhenProviderIdIsNew() {
        when(oidcUser.getSubject()).thenReturn("google-subject");
        when(oidcUser.getEmail()).thenReturn("Lucas.User@email.com");
        when(oidcUser.getFullName()).thenReturn(" Lucas Zimmerman ");
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(userRepository.findByAuthProviderAndProviderId(
                AuthProvider.GOOGLE,
                "google-subject"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("Lucas.User@email.com"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByUsernameIgnoreCase("lucas.user"))
                .thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = service.authenticate(oidcUser);

        assertThat(user.getEmail()).isEqualTo("lucas.user@email.com");
        assertThat(user.getName()).isEqualTo("Lucas Zimmerman");
        assertThat(user.getUsername()).isEqualTo("lucas.user");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(user.getProviderId()).isEqualTo("google-subject");
        assertThat(user.getEmailVerified()).isTrue();
        assertThat(user.isActive()).isTrue();
        assertThat(user.getRole()).isEqualTo(UserRole.USER);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue()).isSameAs(user);
    }

    @Test
    void shouldRejectGoogleUserWhenEmailAlreadyBelongsToLocalAccount() {
        when(oidcUser.getSubject()).thenReturn("google-subject");
        when(oidcUser.getEmail()).thenReturn("lucas@email.com");
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(userRepository.findByAuthProviderAndProviderId(
                AuthProvider.GOOGLE,
                "google-subject"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("lucas@email.com"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.authenticate(oidcUser))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessage("Ja existe uma conta associada a este email.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectGoogleUserWhenEmailIsNotVerified() {
        when(oidcUser.getSubject()).thenReturn("google-subject");
        when(oidcUser.getEmail()).thenReturn("lucas@email.com");
        when(oidcUser.getEmailVerified()).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(oidcUser))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessage("O email da conta Google nao esta verificado.");

        verify(userRepository, never()).save(any(User.class));
    }
}
