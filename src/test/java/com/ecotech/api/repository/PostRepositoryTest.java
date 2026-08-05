package com.ecotech.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import com.ecotech.api.config.JpaAuditingConfig;
import com.ecotech.api.model.Post;
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
class PostRepositoryTest {

    @Autowired
    private PostRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User createUser(String username) {
        User user = new User();

        user.setUsername(username);
        user.setEmail(username + "@email.com");
        user.setPassword("123456");
        user.setName(username);
        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }

    private Post createPost(User user, String content) {
        Post post = new Post();

        post.setUser(user);
        post.setContent(content);

        return repository.save(post);
    }

    @Test
    void shouldFindByUserId() {
        User user = createUser("lucas");
        User anotherUser = createUser("maria");
        Post post = createPost(user, "Post do Lucas.");
        createPost(anotherUser, "Post da Maria.");

        Page<Post> result = repository.findByUserId(
                user.getId(),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(post);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnTrueWhenPostExistsByIdAndUserId() {
        User user = createUser("lucas");
        Post post = createPost(user, "Post do Lucas.");

        assertThat(repository.existsByIdAndUserId(
                post.getId(),
                user.getId()))
                .isTrue();
    }

    @Test
    void shouldReturnFalseWhenPostDoesNotExistByIdAndUserId() {
        User user = createUser("lucas");
        User anotherUser = createUser("maria");
        Post post = createPost(user, "Post do Lucas.");

        assertThat(repository.existsByIdAndUserId(
                post.getId(),
                anotherUser.getId()))
                .isFalse();
    }

    @Test
    void shouldFindByIdWithUserEntityGraph() {
        User user = createUser("lucas");
        Post post = createPost(user, "Post do Lucas.");

        entityManager.flush();
        entityManager.clear();

        Post result = repository.findById(post.getId()).orElseThrow();

        assertThat(result.getUser().getId()).isEqualTo(user.getId());

        entityManager.detach(result);
        entityManager.detach(result.getUser());

        assertThatCode(() -> result.getUser().getUsername())
                .doesNotThrowAnyException();

        assertThat(result.getUser().getUsername())
                .isEqualTo(user.getUsername());
    }

    @Test
    void shouldFindAllWithUserEntityGraph() {
        User user = createUser("lucas");
        createPost(user, "Post do Lucas.");

        entityManager.flush();
        entityManager.clear();

        Page<Post> result = repository.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);

        Post post = result.getContent().getFirst();

        assertThat(post.getUser().getId()).isEqualTo(user.getId());

        entityManager.detach(post);
        entityManager.detach(post.getUser());

        assertThatCode(() -> post.getUser().getUsername())
                .doesNotThrowAnyException();

        assertThat(post.getUser().getUsername())
                .isEqualTo(user.getUsername());
    }

    @Test
    void shouldFindByUserIdWithUserEntityGraph() {
        User user = createUser("lucas");
        createPost(user, "Post do Lucas.");

        entityManager.flush();
        entityManager.clear();

        Page<Post> result = repository.findByUserId(
                user.getId(),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);

        Post post = result.getContent().getFirst();

        assertThat(post.getUser().getId()).isEqualTo(user.getId());

        entityManager.detach(post);
        entityManager.detach(post.getUser());

        assertThatCode(() -> post.getUser().getUsername())
                .doesNotThrowAnyException();

        assertThat(post.getUser().getUsername())
                .isEqualTo(user.getUsername());
    }

    @Test
    void shouldPaginateFindAll() {
        User user = createUser("lucas");

        createPost(user, "Primeiro post.");
        createPost(user, "Segundo post.");
        createPost(user, "Terceiro post.");

        entityManager.flush();
        entityManager.clear();

        Page<Post> result = repository.findAll(PageRequest.of(1, 2));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldPaginateFindByUserId() {
        User user = createUser("lucas");
        User anotherUser = createUser("maria");

        createPost(user, "Primeiro post.");
        createPost(user, "Segundo post.");
        createPost(user, "Terceiro post.");
        createPost(anotherUser, "Post de outro usuario.");

        entityManager.flush();
        entityManager.clear();

        Page<Post> result = repository.findByUserId(
                user.getId(),
                PageRequest.of(1, 2));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldReturnFalseWhenPostDoesNotExist() {
        assertThat(repository.existsByIdAndUserId(
                UUID.randomUUID(),
                UUID.randomUUID()))
                .isFalse();
    }
}
