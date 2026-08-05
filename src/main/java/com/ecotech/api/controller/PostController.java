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
import com.ecotech.api.controller.dto.CreatePostDTO;
import com.ecotech.api.controller.dto.PostResponseDTO;
import com.ecotech.api.controller.dto.UpdatePostDTO;
import com.ecotech.api.controller.mappers.PostMapper;
import com.ecotech.api.model.Post;
import com.ecotech.api.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController implements GenericController {

    private final PostService postService;
    private final PostMapper postMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> create(
            @RequestBody @Valid CreatePostDTO createPostDTO,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        Post post = postMapper.toEntity(createPostDTO);

        Post savedPost = postService.save(post, authenticatedUserId);

        URI location = generateLocationHeader(savedPost.getId());

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponseDTO> getPostById(@PathVariable UUID id) {
        Post post = postService.findById(id);

        PostResponseDTO response = postMapper.toResponseDTO(post);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PostResponseDTO>> getAllPosts(Pageable pageable) {
        Page<PostResponseDTO> response = postService
                .findAll(pageable)
                .map(postMapper::toResponseDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PostResponseDTO>> getPostsByUserId(
            @PathVariable UUID userId, Pageable pageable) {
        Page<PostResponseDTO> response = postService
                .findByUserId(userId, pageable)
                .map(postMapper::toResponseDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @postAuthorization.isOwner(#id, authentication)")
    public ResponseEntity<Void> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePostDTO updatePostDTO,
            Authentication authentication) {

        Post post = postService.findById(id);

        postMapper.updateEntity(updatePostDTO, post);

        postService.update(post);

        return ResponseEntity.noContent().build();
    };

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @postAuthorization.isOwner(#id, authentication)")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication) {

        Post post = postService.findById(id);

        postService.delete(post);

        return ResponseEntity.noContent().build();
    }

}
