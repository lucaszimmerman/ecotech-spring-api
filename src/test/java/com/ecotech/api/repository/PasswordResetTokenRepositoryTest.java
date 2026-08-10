package com.ecotech.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.ecotech.api.config.JpaAuditingConfig;
import com.ecotech.api.model.PasswordResetToken;
import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.UserRole;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaAuditingConfig.class)
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindByTokenHash() {
        User user = createUser("lucas");
        PasswordResetToken token = createToken(user, "token-hash");

        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByTokenHash("token-hash"))
                .isPresent()
                .get()
                .extracting(PasswordResetToken::getId)
                .isEqualTo(token.getId());
    }

    @Test
    void shouldDeleteByUserId() {
        User user = createUser("lucas");
        User anotherUser = createUser("maria");
        createToken(user, "token-hash");
        PasswordResetToken anotherToken = createToken(anotherUser, "another-token-hash");

        repository.deleteByUserId(user.getId());

        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByTokenHash("token-hash")).isEmpty();
        assertThat(repository.findByTokenHash("another-token-hash"))
                .isPresent()
                .get()
                .extracting(PasswordResetToken::getId)
                .isEqualTo(anotherToken.getId());
    }

    private User createUser(String username) {
        User user = new User();

        user.setUsername(username);
        user.setEmail(username + "@email.com");
        user.setPassword("123456");
        user.setName(username);
        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }

    private PasswordResetToken createToken(User user, String tokenHash) {
        PasswordResetToken token = new PasswordResetToken();

        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        return repository.save(token);
    }
}
