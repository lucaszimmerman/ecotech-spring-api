package com.ecotech.api.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.controller.dto.PostInteractionSummaryDTO;
import com.ecotech.api.repository.CommentRepository;
import com.ecotech.api.repository.PostLikeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostInteractionService {

    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Map<UUID, PostInteractionSummaryDTO> summarize(List<UUID> postIds, UUID userId) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        // Aggregate the entire page in three queries, regardless of its size.
        Map<UUID, Long> likes = new HashMap<>();
        postLikeRepository.countByPostIds(postIds)
                .forEach(count -> likes.put(count.getPostId(), count.getTotal()));
        Map<UUID, Long> comments = new HashMap<>();
        commentRepository.countByPostIds(postIds)
                .forEach(count -> comments.put(count.getPostId(), count.getTotal()));
        var likedPostIds = new HashSet<>(postLikeRepository.findLikedPostIds(userId, postIds));

        Map<UUID, PostInteractionSummaryDTO> summaries = new HashMap<>();
        postIds.forEach(id -> summaries.put(id, new PostInteractionSummaryDTO(
                likes.getOrDefault(id, 0L), comments.getOrDefault(id, 0L), likedPostIds.contains(id))));
        return summaries;
    }
}
