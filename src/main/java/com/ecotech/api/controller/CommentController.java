package com.ecotech.api.controller;

import java.net.URI;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecotech.api.controller.common.GenericController;
import com.ecotech.api.controller.dto.CommentResponseDTO;
import com.ecotech.api.controller.dto.CreateCommentDTO;
import com.ecotech.api.controller.dto.UpdateCommentDTO;
import com.ecotech.api.controller.mappers.CommentMapper;
import com.ecotech.api.model.Comment;
import com.ecotech.api.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController implements GenericController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> create(
            @RequestBody @Valid CreateCommentDTO createCommentDTO,
            @PathVariable UUID postId,
            Authentication authentication) {

        UUID authenticatedUserId =
                UUID.fromString(authentication.getName());

        Comment comment =
                commentMapper.toEntity(createCommentDTO);

        Comment savedComment =
                commentService.save(
                        comment,
                        authenticatedUserId,
                        postId
                );

        URI location =
                generateLocationHeader(savedComment.getId());

        return ResponseEntity
                .created(location)
                .build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CommentResponseDTO>> getCommentsByPostId(
            @PathVariable UUID postId,
            Pageable pageable) {

        Page<CommentResponseDTO> response =
                commentService
                        .findByPostId(postId, pageable)
                        .map(commentMapper::toResponseDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countByPostId(
            @PathVariable UUID postId) {

        long count =
                commentService.countByPostId(postId);

        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDTO> getCommentById(
            @PathVariable UUID postId,
            @PathVariable UUID id) {

        Comment comment =
                commentService.findByIdAndPostId(id, postId);

        CommentResponseDTO response =
                commentMapper.toResponseDTO(comment);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize(
        "hasRole('ADMIN') or @commentAuthorization.isOwner(#id, #postId, authentication)"
    )
    public ResponseEntity<Void> update(
            @PathVariable UUID postId,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCommentDTO updateCommentDTO) {

        Comment comment =
                commentService.findByIdAndPostId(id, postId);

        commentMapper.updateEntity(
                updateCommentDTO,
                comment
        );

        commentService.update(comment);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
        "hasRole('ADMIN') or @commentAuthorization.isOwner(#id, #postId, authentication)"
    )
    public ResponseEntity<Void> delete(
            @PathVariable UUID postId,
            @PathVariable UUID id) {

        Comment comment =
                commentService.findByIdAndPostId(id, postId);

        commentService.delete(comment);

        return ResponseEntity.noContent().build();
    }
}
