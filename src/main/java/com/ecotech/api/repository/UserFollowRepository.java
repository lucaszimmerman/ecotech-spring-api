package com.ecotech.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecotech.api.model.UserFollow;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

        boolean existsByFollowerIdAndFollowedId(
                        UUID followerId,
                        UUID followedId);

        Optional<UserFollow> findByFollowerIdAndFollowedId(
                        UUID followerId,
                        UUID followedId);

        @Query("""
                            SELECT uf
                            FROM UserFollow uf
                            JOIN FETCH uf.followed
                            WHERE uf.follower.id = :followerId
                        """)
        Page<UserFollow> findByFollowerId(
                        @Param("followerId") UUID followerId,
                        Pageable pageable);

        @Query("""
                            SELECT uf
                            FROM UserFollow uf
                            JOIN FETCH uf.follower
                            WHERE uf.followed.id = :followedId
                        """)
        Page<UserFollow> findByFollowedId(
                        @Param("followedId") UUID followedId,
                        Pageable pageable);

}
