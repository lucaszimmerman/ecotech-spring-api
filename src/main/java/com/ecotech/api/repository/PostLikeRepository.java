package com.ecotech.api.repository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecotech.api.model.PostLike;
import com.ecotech.api.repository.projections.PostCount;

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

    @Query("""
            SELECT pl.post.id AS postId, COUNT(pl) AS total
            FROM PostLike pl WHERE pl.post.id IN :postIds
            GROUP BY pl.post.id
            """)
    List<PostCount> countByPostIds(@Param("postIds") Collection<UUID> postIds);

    @Query("""
            SELECT pl.post.id FROM PostLike pl
            WHERE pl.user.id = :userId AND pl.post.id IN :postIds
            """)
    List<UUID> findLikedPostIds(@Param("userId") UUID userId,
            @Param("postIds") Collection<UUID> postIds);
}
