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

import com.ecotech.api.config.AppProperties;
import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.EmailVerificationToken;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.EmailVerificationTokenRepository;
import com.ecotech.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(
                tokenRepository,
                userRepository,
                emailService,
                new AppProperties("http://localhost:8080"));
    }

    @Test
    void shouldSendVerificationEmailWithStoredTokenHash() {
        User user = createUser(false);
        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);

        service.sendVerification(user);

        verify(tokenRepository).deleteByUserId(user.getId());
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendEmailVerification(
                org.mockito.ArgumentMatchers.eq(user.getEmail()),
                linkCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();
        String rawToken = extractToken(linkCaptor.getValue());

        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getTokenHash()).isEqualTo(hashToken(rawToken));
        assertThat(savedToken.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
        assertThat(savedToken.getExpiresAt()).isBefore(LocalDateTime.now().plusMinutes(31));
        assertThat(linkCaptor.getValue())
                .startsWith("http://localhost:8080/auth/verify-email?token=");
    }

    @Test
    void shouldVerifyEmailSuccessfully() {
        String rawToken = "valid-token";
        User user = createUser(false);
        EmailVerificationToken verificationToken = createToken(
                user,
                LocalDateTime.now().plusMinutes(10),
                null);

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(verificationToken));

        service.verifyEmail(rawToken);

        verify(tokenRepository).findByTokenHash(hashToken(rawToken));

        assertThat(user.getEmailVerified()).isTrue();
        assertThat(verificationToken.getUsedAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenTokenDoesNotExist() {
        String rawToken = "invalid-token";

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyEmail(rawToken))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("inv");

        verify(tokenRepository).findByTokenHash(hashToken(rawToken));
        verifyNoInteractions(userRepository, emailService);
    }

    @Test
    void shouldThrowExceptionWhenTokenWasAlreadyUsed() {
        String rawToken = "used-token";
        User user = createUser(false);
        LocalDateTime usedAt = LocalDateTime.now().minusMinutes(1);
        EmailVerificationToken verificationToken = createToken(
                user,
                LocalDateTime.now().plusMinutes(10),
                usedAt);

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(verificationToken));

        assertThatThrownBy(() -> service.verifyEmail(rawToken))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("utilizado");

        verify(tokenRepository).findByTokenHash(hashToken(rawToken));

        assertThat(user.getEmailVerified()).isFalse();
        assertThat(verificationToken.getUsedAt()).isEqualTo(usedAt);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {
        String rawToken = "expired-token";
        User user = createUser(false);
        EmailVerificationToken verificationToken = createToken(
                user,
                LocalDateTime.now().minusMinutes(1),
                null);

        when(tokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(verificationToken));

        assertThatThrownBy(() -> service.verifyEmail(rawToken))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("expirou");

        verify(tokenRepository).findByTokenHash(hashToken(rawToken));

        assertThat(user.getEmailVerified()).isFalse();
        assertThat(verificationToken.getUsedAt()).isNull();
    }

    @Test
    void shouldResendVerificationWhenUserIsNotVerified() {
        User user = createUser(false);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        service.resendVerification(user.getId());

        verify(userRepository).findById(user.getId());
        verify(tokenRepository).deleteByUserId(user.getId());
        verify(tokenRepository).save(org.mockito.ArgumentMatchers.any(
                EmailVerificationToken.class));
        verify(emailService).sendEmailVerification(
                org.mockito.ArgumentMatchers.eq(user.getEmail()),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldThrowExceptionWhenResendingToVerifiedUser() {
        User user = createUser(true);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.resendVerification(user.getId()))
                .isInstanceOf(CampoInvalidoException.class)
                .hasMessageContaining("confirmado");

        verify(userRepository).findById(user.getId());
        verify(tokenRepository, never()).deleteByUserId(user.getId());
        verify(tokenRepository, never()).save(org.mockito.ArgumentMatchers.any(
                EmailVerificationToken.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldThrowExceptionWhenResendingToUnknownUser() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resendVerification(userId))
                .isInstanceOf(RegistroNaoEncontradoException.class)
                .hasMessageContaining("n");

        verify(userRepository).findById(userId);
        verifyNoInteractions(tokenRepository, emailService);
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

    private EmailVerificationToken createToken(
            User user,
            LocalDateTime expiresAt,
            LocalDateTime usedAt) {

        EmailVerificationToken token = new EmailVerificationToken();

        token.setUser(user);
        token.setExpiresAt(expiresAt);
        token.setUsedAt(usedAt);

        return token;
    }

    private String extractToken(String verificationLink) {
        URI uri = URI.create(verificationLink);
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
