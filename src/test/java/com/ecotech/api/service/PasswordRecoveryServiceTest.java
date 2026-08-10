package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
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

import com.ecotech.api.config.AppProperties;
import com.ecotech.api.controller.dto.auth.ResetPasswordDTO;
import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.PasswordResetToken;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.PasswordResetTokenRepository;
import com.ecotech.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    private PasswordEncoder passwordEncoder;

    private PasswordRecoveryService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
        service = new PasswordRecoveryService(
                tokenRepository,
                userRepository,
                emailService,
                new AppProperties("http://localhost:8080"),
                passwordEncoder);
    }

    @Test
    void shouldSendPasswordResetEmailWithStoredTokenHash() {
        User user = createUser(true);
        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findByEmailIgnoreCase("lucas@email.com"))
                .thenReturn(Optional.of(user));

        service.requestPasswordReset("  LUCAS@email.com  ");

        verify(userRepository).findByEmailIgnoreCase("lucas@email.com");
        verify(tokenRepository).deleteByUserId(user.getId());
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendPasswordResetEmail(
                org.mockito.ArgumentMatchers.eq(user.getEmail()),
                linkCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        String rawToken = extractToken(linkCaptor.getValue());

        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getTokenHash()).isEqualTo(hashToken(rawToken));
        assertThat(savedToken.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
        assertThat(savedToken.getExpiresAt()).isBefore(LocalDateTime.now().plusMinutes(31));
        assertThat(linkCaptor.getValue())
                .startsWith("http://localhost:8080/auth/reset-password?token=");
    }

    @Test
    void shouldNotRevealUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("unknown@email.com"))
                .thenReturn(Optional.empty());

        service.requestPasswordReset("unknown@email.com");

        verify(userRepository).findByEmailIgnoreCase("unknown@email.com");
        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void shouldNotSendResetToUnverifiedUser() {
        User user = createUser(false);

        when(userRepository.findByEmailIgnoreCase(user.getEmail()))
                .thenReturn(Optional.of(user));

        service.requestPasswordReset(user.getEmail());

        verify(userRepository).findByEmailIgnoreCase(user.getEmail());
        verify(tokenRepository, never()).deleteByUserId(user.getId());
        verify(tokenRepository, never()).save(org.mockito.ArgumentMatchers.any(
                PasswordResetToken.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldResetPasswordSuccessfullyAndMarkTokenAsUsed() {
        String rawToken = "valid-token";
        User user = createUser(true);
        user.setPassword(passwordEncoder.encode("old-password"));
        PasswordResetToken token = createToken(
                user,
                LocalDateTime.now().plusMinutes(10),
                null);

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(token));

        service.resetPassword(new ResetPasswordDTO(
                rawToken,
                "new-password",
                "new-password"));

        verify(tokenRepository).findByTokenHash(hashToken(rawToken));

        assertThat(passwordEncoder.matches("new-password", user.getPassword())).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    void shouldNotReuseTokenAfterSuccessfulPasswordReset() {
        String rawToken = "valid-token";
        User user = createUser(true);
        user.setPassword(passwordEncoder.encode("old-password"));
        PasswordResetToken token = createToken(
                user,
                LocalDateTime.now().plusMinutes(10),
                null);

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(token));

        service.resetPassword(new ResetPasswordDTO(
                rawToken,
                "new-password",
                "new-password"));

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordDTO(
                rawToken,
                "another-password",
                "another-password")))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("utilizado");

        assertThat(passwordEncoder.matches("new-password", user.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("another-password", user.getPassword())).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenTokenDoesNotExist() {
        String rawToken = "invalid-token";

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordDTO(
                rawToken,
                "new-password",
                "new-password")))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("invalido");

        verify(tokenRepository).findByTokenHash(hashToken(rawToken));
        verifyNoInteractions(userRepository, emailService);
    }

    @Test
    void shouldThrowExceptionWhenTokenWasAlreadyUsed() {
        String rawToken = "used-token";
        User user = createUser(true);
        LocalDateTime usedAt = LocalDateTime.now().minusMinutes(1);
        PasswordResetToken token = createToken(
                user,
                LocalDateTime.now().plusMinutes(10),
                usedAt);

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordDTO(
                rawToken,
                "new-password",
                "new-password")))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("utilizado");

        assertThat(token.getUsedAt()).isEqualTo(usedAt);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {
        String rawToken = "expired-token";
        User user = createUser(true);
        PasswordResetToken token = createToken(
                user,
                LocalDateTime.now().minusMinutes(1),
                null);

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordDTO(
                rawToken,
                "new-password",
                "new-password")))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("expirou");

        assertThat(token.getUsedAt()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenPasswordConfirmationDoesNotMatch() {
        String rawToken = "valid-token";
        User user = createUser(true);
        user.setPassword(passwordEncoder.encode("old-password"));
        PasswordResetToken token = createToken(
                user,
                LocalDateTime.now().plusMinutes(10),
                null);

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordDTO(
                rawToken,
                "new-password",
                "another-password")))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("nao confere");

        assertThat(passwordEncoder.matches("old-password", user.getPassword())).isTrue();
        assertThat(token.getUsedAt()).isNull();
    }

    private User createUser(boolean emailVerified) {
        User user = new User();

        user.setId(UUID.randomUUID());
        user.setUsername("lucas");
        user.setEmail("lucas@email.com");
        user.setName("Lucas Zimmerman");
        user.setEmailVerified(emailVerified);

        return user;
    }

    private PasswordResetToken createToken(
            User user,
            LocalDateTime expiresAt,
            LocalDateTime usedAt) {

        PasswordResetToken token = new PasswordResetToken();

        token.setUser(user);
        token.setExpiresAt(expiresAt);
        token.setUsedAt(usedAt);

        return token;
    }

    private String extractToken(String resetLink) {
        URI uri = URI.create(resetLink);
        String query = uri.getRawQuery();

        assertThat(query).startsWith("token=");

        return URLDecoder.decode(
                query.substring("token=".length()),
                StandardCharsets.UTF_8);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
