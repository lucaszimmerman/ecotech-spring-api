package com.ecotech.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecotech.api.model.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    Optional<PostLike> findByUserIdAndPostId(UUID userId, UUID postId);

    @Query("""
            SELECT pl
            FROM PostLike pl
            JOIN FETCH pl.user
            WHERE pl.post.id = :postId
            """)
    Page<PostLike> findByPostId(
            @Param("postId") UUID postId,
            Pageable pageable);

    long countByPostId(UUID postId);
}
