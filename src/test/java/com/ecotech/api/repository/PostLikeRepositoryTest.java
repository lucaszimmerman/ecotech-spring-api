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
import com.ecotech.api.model.PostLike;
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
class PostLikeRepositoryTest {

    @Autowired
    private PostLikeRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

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

    private Post createPost(User user) {
        Post post = new Post();

        post.setUser(user);
        post.setContent("Conteudo valido do post.");

        return postRepository.save(post);
    }

    private PostLike createPostLike(User user, Post post) {
        PostLike postLike = new PostLike();

        postLike.setUser(user);
        postLike.setPost(post);

        return repository.save(postLike);
    }

    @Test
    void shouldFindByPostIdWithUserJoinFetch() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        createPostLike(user, post);

        entityManager.flush();
        entityManager.clear();

        Page<PostLike> result = repository.findByPostId(
                post.getId(),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        PostLike postLike = result.getContent().getFirst();

        assertThat(postLike.getUser().getId()).isEqualTo(user.getId());

        entityManager.detach(postLike);
        entityManager.detach(postLike.getUser());

        assertThatCode(() -> postLike.getUser().getUsername())
                .doesNotThrowAnyException();

        assertThat(postLike.getUser().getUsername())
                .isEqualTo(user.getUsername());
    }

    @Test
    void shouldPaginateFindByPostId() {
        User author = createUser("author");
        Post post = createPost(author);
        User lucas = createUser("lucas");
        User maria = createUser("maria");
        User ana = createUser("ana");

        createPostLike(lucas, post);
        createPostLike(maria, post);
        createPostLike(ana, post);

        entityManager.flush();
        entityManager.clear();

        Page<PostLike> result = repository.findByPostId(
                post.getId(),
                PageRequest.of(1, 2));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldFindByUserIdAndPostId() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        PostLike postLike = createPostLike(user, post);

        assertThat(repository.findByUserIdAndPostId(
                user.getId(),
                post.getId()))
                .contains(postLike);
    }

    @Test
    void shouldReturnTrueWhenPostLikeExists() {
        User user = createUser("lucas");
        Post post = createPost(createUser("maria"));
        createPostLike(user, post);

        assertThat(repository.existsByUserIdAndPostId(
                user.getId(),
                post.getId()))
                .isTrue();
    }

    @Test
    void shouldReturnFalseWhenPostLikeDoesNotExist() {
        assertThat(repository.existsByUserIdAndPostId(
                UUID.randomUUID(),
                UUID.randomUUID()))
                .isFalse();
    }

    @Test
    void shouldCountByPostId() {
        User author = createUser("author");
        Post post = createPost(author);

        createPostLike(createUser("lucas"), post);
        createPostLike(createUser("maria"), post);

        assertThat(repository.countByPostId(post.getId()))
                .isEqualTo(2L);
    }
}
