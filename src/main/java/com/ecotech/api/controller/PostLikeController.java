package com.ecotech.api.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecotech.api.controller.dto.PostLikeStatusDTO;
import com.ecotech.api.controller.dto.UserSummaryDTO;
import com.ecotech.api.controller.mappers.UserMapper;
import com.ecotech.api.model.PostLike;
import com.ecotech.api.service.PostLikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final UserMapper userMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> like(
            @PathVariable UUID postId,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        postLikeService.like(authenticatedUserId, postId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlike(
            @PathVariable UUID postId,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        postLikeService.unlike(authenticatedUserId, postId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserSummaryDTO>> findByPostId(
            @PathVariable UUID postId,
            Pageable pageable) {
        Page<UserSummaryDTO> users = postLikeService
                .findByPostId(postId, pageable)
                .map(PostLike::getUser)
                .map(userMapper::toSummaryDTO);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostLikeStatusDTO> status(
            @PathVariable UUID postId,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        long likeCount = postLikeService.countByPostId(postId);
        boolean likedByCurrentUser = postLikeService
                .likedByUser(authenticatedUserId, postId);

        PostLikeStatusDTO response = new PostLikeStatusDTO(
                likeCount,
                likedByCurrentUser);

        return ResponseEntity.ok(response);
    }
}
