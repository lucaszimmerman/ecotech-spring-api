package com.ecotech.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final PostImageService postImageService;

    @Transactional
    public Post save(Post post, UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado."));

        post.setUser(user);
        validator.validatePost(post);

        Post savedPost = repository.save(post);

        postImageService.uploadImage(savedPost, file);

        return savedPost;
    }

    @Transactional
    public Post update(Post post) {
        validator.validatePost(post);
        return repository.save(post);
    }

    @Transactional
    public Post update(Post post, MultipartFile file, boolean removeImage) {
        validator.validatePost(post);

        String imageUrlToDelete = postImageService.prepareImageUpdate(
                post,
                file,
                removeImage);

        Post updatedPost = repository.save(post);

        postImageService.deleteImage(imageUrlToDelete);

        return updatedPost;
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
        String imageUrl = post.getImageUrl();

        repository.delete(post);

        postImageService.deleteImage(imageUrl);
    }


}
