package com.ecotech.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.PostRepository;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.PostValidator;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository repository;
    private final PostValidator validator;
    private final UserRepository userRepository;

    @Transactional
    public Post save(Post post, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado."));

        post.setUser(user);
        validator.validatePost(post);
        return repository.save(post);
    }

    @Transactional
    public Post update(Post post) {
        validator.validatePost(post);
        return repository.save(post); 
    }

    @Transactional(readOnly = true) 
    public Post findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Post não encontrado."));
    }

    @Transactional(readOnly = true) 
    public Page<Post> findByUserId(UUID userId, Pageable pageable){
        return repository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true) 
    public Page<Post> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional
    public void delete(Post post) {
        repository.delete(post);
    }


    
}
