package com.ecotech.api.security;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.ecotech.api.controller.dto.auth.LoginResponseDTO;

/**
 * Guarda, por poucos segundos, o resultado do login Google sob um codigo
 * aleatorio de uso unico. O navegador recebe apenas o codigo na URL de retorno
 * e o troca pelo JWT em POST /auth/google/exchange, de modo que o token nunca
 * aparece na URL nem no historico.
 *
 * O armazenamento e em memoria: funciona com uma instancia da API. Com varias
 * instancias, troque por um armazenamento compartilhado (ex.: banco ou Redis).
 */
@Component
public class GoogleLoginCodeStore {

    static final Duration TTL = Duration.ofSeconds(60);

    private record Entry(LoginResponseDTO response, Instant expiresAt) {
    }

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Clock clock;

    public GoogleLoginCodeStore() {
        this(Clock.systemUTC());
    }

    GoogleLoginCodeStore(Clock clock) {
        this.clock = clock;
    }

    public String issue(LoginResponseDTO response) {
        Instant now = clock.instant();
        entries.values().removeIf(entry -> entry.expiresAt().isBefore(now));

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        entries.put(code, new Entry(response, now.plus(TTL)));
        return code;
    }

    public Optional<LoginResponseDTO> consume(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        Entry entry = entries.remove(code);
        if (entry == null || entry.expiresAt().isBefore(clock.instant())) {
            return Optional.empty();
        }
        return Optional.of(entry.response());
    }
}
