package com.ecotech.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecotech.api.model.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {

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
