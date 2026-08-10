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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(30);

    private static final int TOKEN_SIZE_BYTES = 32;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Transactional
    public void sendVerification(User user) {
        log.debug("Gerando token de verificacao de email. userId={}", user.getId());

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
        log.info("Email de verificacao solicitado ao provedor. userId={}", user.getId());
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        log.debug("Iniciando confirmacao de email.");

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new CampoInvalidoException(
                        "token",
                        "Token de verificação inválido."));

        if (verificationToken.getUsedAt() != null) {
            log.warn(
                    "Confirmacao de email recusada: token ja utilizado. userId={}",
                    verificationToken.getUser().getId()
            );
            throw new CampoInvalidoException(
                    "token",
                    "Este token já foi utilizado.");
        }

        if (verificationToken
                .getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            log.warn(
                    "Confirmacao de email recusada: token expirado. userId={}",
                    verificationToken.getUser().getId()
            );
            throw new CampoInvalidoException(
                    "token",
                    "O token de verificação expirou.");
        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);

        verificationToken.setUsedAt(
                LocalDateTime.now());
        log.info("Email confirmado com sucesso. userId={}", user.getId());
    }

    @Transactional
    public void resendVerification(UUID userId) {
        log.debug("Iniciando reenvio de verificacao de email. userId={}", userId);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Usuário não encontrado."));

        if (Boolean.TRUE.equals(
                user.getEmailVerified())) {
            log.warn("Reenvio de verificacao recusado: email ja confirmado. userId={}", userId);
            throw new CampoInvalidoException(
                    "email",
                    "O email já foi confirmado.");
        }

        sendVerification(user);
        log.info("Reenvio de verificacao concluido. userId={}", userId);
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
