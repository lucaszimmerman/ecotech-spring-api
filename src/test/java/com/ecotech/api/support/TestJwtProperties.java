package com.ecotech.api.support;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.test.context.DynamicPropertyRegistry;

public final class TestJwtProperties {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TestJwtProperties() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        registry.add("security.jwt.secret-key", TestJwtProperties::generateSecretKey);
    }

    private static String generateSecretKey() {
        byte[] key = new byte[32];
        SECURE_RANDOM.nextBytes(key);

        return Base64.getEncoder().encodeToString(key);
    }
}
