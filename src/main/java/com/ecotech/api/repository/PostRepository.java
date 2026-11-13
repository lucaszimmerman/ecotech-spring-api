package com.ecotech.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecotech.api.model.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {

    @EntityGraph(attributePaths = "user")
    @Query("""
            SELECT p FROM Post p
            WHERE p.user.active = true
              AND EXISTS (SELECT f.id FROM UserFollow f
                          WHERE f.follower.id = :userId AND f.followed.id = p.user.id)
            """)
    Page<Post> findFeed(@Param("userId") UUID userId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Post> findById(UUID id);

    boolean existsByIdAndUserId(
            UUID postId,
            UUID userId);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<Post> findByUserId(
            UUID userId,
            Pageable pageable);

}
