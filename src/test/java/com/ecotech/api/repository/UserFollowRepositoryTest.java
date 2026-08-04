package com.ecotech.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.ecotech.api.config.JpaAuditingConfig;
import com.ecotech.api.model.User;
import com.ecotech.api.model.UserFollow;
import com.ecotech.api.model.enums.UserRole;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaAuditingConfig.class)
class UserFollowRepositoryTest {

    @Autowired
    private UserFollowRepository repository;

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

    private UserFollow createUserFollow(User follower, User followed) {
        UserFollow userFollow = new UserFollow();

        userFollow.setFollower(follower);
        userFollow.setFollowed(followed);

        return repository.save(userFollow);
    }

    @Test
    void shouldFindByFollowerIdWithFollowedJoinFetch() {
        User follower = createUser("lucas");
        User followed = createUser("maria");
        createUserFollow(follower, followed);

        entityManager.flush();
        entityManager.clear();

        Page<UserFollow> result = repository.findByFollowerId(
                follower.getId(),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        UserFollow userFollow = result.getContent().getFirst();

        assertThat(userFollow.getFollowed().getId()).isEqualTo(followed.getId());

        entityManager.detach(userFollow);
        entityManager.detach(userFollow.getFollowed());

        assertThatCode(() -> userFollow.getFollowed().getUsername())
                .doesNotThrowAnyException();

        assertThat(userFollow.getFollowed().getUsername())
                .isEqualTo(followed.getUsername());
    }

    @Test
    void shouldFindByFollowedIdWithFollowerJoinFetch() {
        User follower = createUser("lucas");
        User followed = createUser("maria");
        createUserFollow(follower, followed);

        entityManager.flush();
        entityManager.clear();

        Page<UserFollow> result = repository.findByFollowedId(
                followed.getId(),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        UserFollow userFollow = result.getContent().getFirst();

        assertThat(userFollow.getFollower().getId()).isEqualTo(follower.getId());

        entityManager.detach(userFollow);
        entityManager.detach(userFollow.getFollower());

        assertThatCode(() -> userFollow.getFollower().getUsername())
                .doesNotThrowAnyException();

        assertThat(userFollow.getFollower().getUsername())
                .isEqualTo(follower.getUsername());
    }

    @Test
    void shouldPaginateFindByFollowerId() {
        User follower = createUser("lucas");
        User maria = createUser("maria");
        User joao = createUser("joao");
        User ana = createUser("ana");

        createUserFollow(follower, maria);
        createUserFollow(follower, joao);
        createUserFollow(follower, ana);

        entityManager.flush();
        entityManager.clear();

        Page<UserFollow> result = repository.findByFollowerId(
                follower.getId(),
                PageRequest.of(1, 2));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldPaginateFindByFollowedId() {
        User followed = createUser("maria");
        User lucas = createUser("lucas");
        User joao = createUser("joao");
        User ana = createUser("ana");

        createUserFollow(lucas, followed);
        createUserFollow(joao, followed);
        createUserFollow(ana, followed);

        entityManager.flush();
        entityManager.clear();

        Page<UserFollow> result = repository.findByFollowedId(
                followed.getId(),
                PageRequest.of(1, 2));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldFindByFollowerIdAndFollowedId() {
        User follower = createUser("lucas");
        User followed = createUser("maria");
        UserFollow userFollow = createUserFollow(follower, followed);

        assertThat(repository.findByFollowerIdAndFollowedId(
                follower.getId(),
                followed.getId()))
                .contains(userFollow);
    }

    @Test
    void shouldReturnFalseWhenUserFollowDoesNotExist() {
        assertThat(repository.existsByFollowerIdAndFollowedId(
                UUID.randomUUID(),
                UUID.randomUUID()))
                .isFalse();
    }
}
