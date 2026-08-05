package com.ecotech.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.PostLike;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.PostLikeRepository;
import com.ecotech.api.repository.PostRepository;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.PostLikeValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository repository;
    private final PostLikeValidator validator;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public void like(UUID userId, UUID postId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);

        PostLike postLike = new PostLike();
        postLike.setUser(user);
        postLike.setPost(post);

        validator.validateLike(postLike);

        repository.save(postLike);
    }

    @Transactional
    public void unlike(UUID userId, UUID postId) {
        repository.findByUserIdAndPostId(userId, postId)
                .ifPresent(repository::delete);
    }

    @Transactional(readOnly = true)
    public Page<PostLike> findByPostId(UUID postId, Pageable pageable) {
        findPostById(postId);

        return repository.findByPostId(postId, pageable);
    }

    @Transactional(readOnly = true)
    public long countByPostId(UUID postId) {
        findPostById(postId);

        return repository.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public boolean likedByUser(UUID userId, UUID postId) {
        findUserById(userId);
        findPostById(postId);

        return repository.existsByUserIdAndPostId(userId, postId);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Usuário não encontrado."));
    }

    private Post findPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Post não encontrado."));
    }
}
