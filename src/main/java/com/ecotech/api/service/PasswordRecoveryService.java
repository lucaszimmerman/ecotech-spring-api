package com.ecotech.api.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.config.AppProperties;
import com.ecotech.api.controller.dto.auth.ResetPasswordDTO;
import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.model.PasswordResetToken;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.PasswordResetTokenRepository;
import com.ecotech.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(30);

    private static final int TOKEN_SIZE_BYTES = 32;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(this::createAndSendPasswordReset);
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO dto) {
        String tokenHash = hashToken(dto.token());

        PasswordResetToken passwordResetToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new CampoInvalidoException(
                        "token",
                        "Token de recuperacao de senha invalido."));

        if (passwordResetToken.getUsedAt() != null) {
            throw new CampoInvalidoException(
                    "token",
                    "Este token ja foi utilizado.");
        }

        if (passwordResetToken
                .getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new CampoInvalidoException(
                    "token",
                    "O token de recuperacao de senha expirou.");
        }

        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new CampoInvalidoException(
                    "confirmPassword",
                    "A confirmacao da nova senha nao confere.");
        }

        User user = passwordResetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(dto.newPassword()));

        passwordResetToken.setUsedAt(
                LocalDateTime.now());
    }

    private void createAndSendPasswordReset(User user) {
        tokenRepository.deleteByUserId(user.getId());

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setUser(user);
        passwordResetToken.setTokenHash(tokenHash);
        passwordResetToken.setExpiresAt(
                LocalDateTime.now()
                        .plus(TOKEN_EXPIRATION));

        tokenRepository.save(passwordResetToken);

        String resetLink = appProperties.baseURL()
                + "/auth/reset-password?token="
                + URLEncoder.encode(
                        rawToken,
                        StandardCharsets.UTF_8);

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                resetLink);
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_SIZE_BYTES];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "Nao foi possivel gerar o hash do token.",
                    e);
        }
    }
}
