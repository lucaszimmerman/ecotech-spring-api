package com.ecotech.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecotech.api.model.EmailVerificationToken;


public interface EmailVerificationTokenRepository 
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(
        String tokenHash
    );

    void deleteByUserId(UUID userId);
}
