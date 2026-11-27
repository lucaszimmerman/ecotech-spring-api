package com.ecotech.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.model.enums.UserRole;

class GoogleLoginCodeStoreTest {

    private final LoginResponseDTO login = new LoginResponseDTO(
            UUID.randomUUID(), "lucas", "Lucas", UserRole.USER, "token", "Bearer", 3600L);

    @Test
    void shouldRejectExpiredUnknownAndBlankCodes() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        MutableClock clock = new MutableClock(start);
        GoogleLoginCodeStore store = new GoogleLoginCodeStore(clock);

        String code = store.issue(login);
        clock.now = start.plus(GoogleLoginCodeStore.TTL).plusSeconds(1);

        assertThat(store.consume(code)).isEmpty();
        assertThat(store.consume("desconhecido")).isEmpty();
        assertThat(store.consume(" ")).isEmpty();
        assertThat(store.consume(null)).isEmpty();
    }

    @Test
    void shouldIssueDistinctUrlSafeCodes() {
        GoogleLoginCodeStore store = new GoogleLoginCodeStore();
        String first = store.issue(login);
        String second = store.issue(login);

        assertThat(first).isNotEqualTo(second).matches("[A-Za-z0-9_-]{43}");
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
