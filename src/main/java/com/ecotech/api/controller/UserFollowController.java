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

import com.ecotech.api.controller.dto.UserSummaryDTO;
import com.ecotech.api.controller.mappers.UserMapper;
import com.ecotech.api.model.UserFollow;
import com.ecotech.api.service.UserFollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final UserMapper userMapper;

    @PostMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> follow(
        @PathVariable UUID id,
        Authentication authentication
    )
    {
        UUID authenticatedUserId =
             UUID.fromString(authentication.getName());

        userFollowService.follow(authenticatedUserId, id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unfollow(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID authenticatedUserId = UUID.fromString(authentication.getName());
        userFollowService.unfollow(authenticatedUserId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/followers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserSummaryDTO>> findFollowers(
            @PathVariable UUID id,
            Pageable pageable) {
        Page<UserSummaryDTO> followers = userFollowService
                .findFollowers(id, pageable)
                .map(UserFollow::getFollower)
                .map(userMapper::toSummaryDTO);

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{id}/following")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserSummaryDTO>> findFollowing(
            @PathVariable UUID id,
            Pageable pageable) {
        Page<UserSummaryDTO> following = userFollowService
                .findFollowing(id, pageable)
                .map(UserFollow::getFollowed)
                .map(userMapper::toSummaryDTO);

        return ResponseEntity.ok(following);
    }
}
