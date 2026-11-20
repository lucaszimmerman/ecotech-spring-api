package com.ecotech.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import com.ecotech.api.config.JpaAuditingConfig;
import com.ecotech.api.controller.dto.PostInteractionSummaryDTO;
import com.ecotech.api.model.Comment;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.PostLike;
import com.ecotech.api.model.User;
import com.ecotech.api.model.UserFollow;
import com.ecotech.api.service.PostInteractionService;

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@Import({JpaAuditingConfig.class, PostInteractionService.class})
class SocialQueriesTest {

    @Autowired private UserRepository users;
    @Autowired private PostRepository posts;
    @Autowired private UserFollowRepository follows;
    @Autowired private PostLikeRepository likes;
    @Autowired private CommentRepository comments;
    @Autowired private PostInteractionService interactions;
    @Autowired private EntityManager entityManager;

    private User user(String username, String name) {
        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded-password");
        return users.save(user);
    }

    private Post post(User author, String content) {
        Post post = new Post();
        post.setUser(author);
        post.setContent(content);
        return posts.save(post);
    }

    private void follow(User follower, User followed) {
        UserFollow follow = new UserFollow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follows.save(follow);
    }

    private void like(User user, Post post) {
        PostLike like = new PostLike();
        like.setUser(user);
        like.setPost(post);
        likes.save(like);
    }

    private void comment(User user, Post post) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent("A useful comment.");
        comments.save(comment);
    }

    @Test
    void shouldSearchByUsernameOrNameIgnoringCaseAndExcludingInactiveUsers() {
        User byUsername = user("ecolucas", "Lucas");
        User byName = user("maria", "Eco Educator");
        user("joao", "Another Person");
        User inactive = user("eco-inactive", "Eco Inactive");
        inactive.setActive(false);

        var page = users.searchActiveUsers("ECO", PageRequest.of(0, 1, Sort.by("username")));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).containsExactly(byUsername);
        assertThat(users.searchActiveUsers("eco", PageRequest.of(1, 1, Sort.by("username")))
                .getContent()).containsExactly(byName);
    }

    @Test
    void shouldTreatSearchWildcardsAsLiteralCharacters() {
        User special = user("eco_percent", "100% Eco");
        user("normal", "Normal Person");

        assertThat(users.searchActiveUsers("%", PageRequest.of(0, 10)).getContent())
                .containsExactly(special);
        assertThat(users.searchActiveUsers("_", PageRequest.of(0, 10)).getContent())
                .containsExactly(special);
        assertThat(users.searchActiveUsers("missing", PageRequest.of(0, 10))).isEmpty();
    }

    @Test
    void shouldDiscoverOnlyActiveUsersWhenSearchIsEmpty() {
        User active = user("active", "Active User");
        User inactive = user("inactive", "Inactive User");
        inactive.setActive(false);

        assertThat(users.searchActiveUsers("", PageRequest.of(0, 10)).getContent())
                .containsExactly(active);
    }

    @Test
    void shouldPaginateFeedInDescendingOrderAndExcludeUnfollowedSelfAndInactiveAuthors() {
        User viewer = user("viewer", "Viewer");
        User followed = user("followed", "Followed");
        User unrelatedViewer = user("unrelated", "Unrelated");
        User stranger = user("stranger", "Stranger");
        User inactive = user("inactive", "Inactive");
        inactive.setActive(false);
        follow(viewer, followed);
        follow(viewer, inactive);
        follow(unrelatedViewer, stranger);
        Post older = post(followed, "Older post");
        Post newer = post(followed, "Newer post");
        post(viewer, "Own post");
        post(stranger, "Stranger post");
        post(inactive, "Inactive post");
        entityManager.flush();
        entityManager.createQuery("UPDATE Post p SET p.createdAt = :createdAt WHERE p.id = :id")
                .setParameter("createdAt", LocalDateTime.of(2020, 1, 1, 0, 0))
                .setParameter("id", older.getId()).executeUpdate();
        entityManager.clear();

        var first = posts.findFeed(viewer.getId(), PageRequest.of(0, 1,
                Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        assertThat(first.getTotalElements()).isEqualTo(2);
        assertThat(first.getContent()).extracting(Post::getId).containsExactly(newer.getId());
        assertThat(posts.findFeed(viewer.getId(), PageRequest.of(1, 1,
                Sort.by(Sort.Direction.DESC, "createdAt", "id"))).getContent())
                .extracting(Post::getId).containsExactly(older.getId());

        Post loaded = first.getContent().getFirst();
        entityManager.detach(loaded);
        entityManager.detach(loaded.getUser());
        assertThat(loaded.getUser().getUsername()).isEqualTo("followed");
    }

    @Test
    void shouldReturnEmptyFeedWhenNotFollowingAnyoneAndAfterUnfollowing() {
        User viewer = user("viewer", "Viewer");
        User author = user("author", "Author");
        post(author, "Post");
        assertThat(posts.findFeed(viewer.getId(), PageRequest.of(0, 10))).isEmpty();

        follow(viewer, author);
        assertThat(posts.findFeed(viewer.getId(), PageRequest.of(0, 10))).hasSize(1);
        follows.deleteAll();
        assertThat(posts.findFeed(viewer.getId(), PageRequest.of(0, 10))).isEmpty();
    }

    @Test
    void shouldSummarizeMultiplePostsInThreeQueriesWithoutMultiplyingCounts() {
        User viewer = user("viewer", "Viewer");
        User other = user("other", "Other");
        Post first = post(other, "First");
        Post second = post(other, "Second");
        Post empty = post(other, "No interactions");
        Post outsidePage = post(other, "Outside page");
        like(viewer, first);
        like(other, first);
        like(other, second);
        like(viewer, outsidePage);
        comment(viewer, first);
        comment(other, first);
        comment(other, second);
        comment(other, outsidePage);
        entityManager.flush();
        entityManager.clear();

        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var result = interactions.summarize(List.of(first.getId(), second.getId(), empty.getId()), viewer.getId());

        assertThat(result).hasSize(3);
        assertThat(result.get(first.getId())).isEqualTo(new PostInteractionSummaryDTO(2, 2, true));
        assertThat(result.get(second.getId())).isEqualTo(new PostInteractionSummaryDTO(1, 1, false));
        assertThat(result.get(empty.getId())).isEqualTo(new PostInteractionSummaryDTO(0, 0, false));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
    }

    @Test
    void shouldNotQueryInteractionsForAnEmptyPage() {
        var statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        assertThat(interactions.summarize(List.of(), UUID.randomUUID())).isEmpty();
        assertThat(statistics.getPrepareStatementCount()).isZero();
    }
}
