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
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.config.AppProperties;
import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.EmailVerificationToken;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.EmailVerificationTokenRepository;
import com.ecotech.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(30);

    private static final int TOKEN_SIZE_BYTES = 32;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Transactional
    public void sendVerification(User user) {

        tokenRepository.deleteByUserId(user.getId());

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken = new EmailVerificationToken();

        verificationToken.setUser(user);
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setExpiresAt(
                LocalDateTime.now()
                        .plus(TOKEN_EXPIRATION));

        tokenRepository.save(verificationToken);

        String verificationLink = appProperties.baseURL()
                + "/auth/verify-email?token="
                + URLEncoder.encode(
                        rawToken,
                        StandardCharsets.UTF_8);

        emailService.sendEmailVerification(
                user.getEmail(),
                verificationLink);
    }

    @Transactional
    public void verifyEmail(String rawToken) {

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new CampoInvalidoException(
                        "token",
                        "Token de verificação inválido."));

        if (verificationToken.getUsedAt() != null) {
            throw new CampoInvalidoException(
                    "token",
                    "Este token já foi utilizado.");
        }

        if (verificationToken
                .getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new CampoInvalidoException(
                    "token",
                    "O token de verificação expirou.");
        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);

        verificationToken.setUsedAt(
                LocalDateTime.now());
    }

    @Transactional
    public void resendVerification(UUID userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Usuário não encontrado."));

        if (Boolean.TRUE.equals(
                user.getEmailVerified())) {
            throw new CampoInvalidoException(
                    "email",
                    "O email já foi confirmado.");
        }

        sendVerification(user);
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
                    "Não foi possível gerar o hash do token.",
                    e);
        }
    }
}
