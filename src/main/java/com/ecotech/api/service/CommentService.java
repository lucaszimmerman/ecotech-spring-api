package com.ecotech.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.Comment;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.CommentRepository;
import com.ecotech.api.repository.PostRepository;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.CommentValidator;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentValidator validator;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment save(Comment comment, UUID userId, UUID postId){
        User user = findUserById(userId);
        Post post = findPostById(postId);

        comment.setUser(user);
        comment.setPost(post);

        validator.validateComment(comment);

        return commentRepository.save(comment);
    }

    
    @Transactional(readOnly = true)
    public Comment findById(UUID id){
        return commentRepository.findById(id)
        .orElseThrow(() ->
        new RegistroNaoEncontradoException("Comentário não encontrado"));
    }

    @Transactional(readOnly = true)
    public Comment findByIdAndPostId(UUID id, UUID postId){
        return commentRepository.findByIdAndPostId(id, postId)
        .orElseThrow(() ->
        new RegistroNaoEncontradoException("Comentário não encontrado."));
    }
    
    @Transactional(readOnly = true)
    public Page<Comment> findByPostId(UUID postId, Pageable pageable){
        findPostById(postId);
        
        return commentRepository.findByPostId(postId, pageable);
    }
    
    @Transactional(readOnly = true)
    public long countByPostId(UUID postId){
        findPostById(postId);
        
        return commentRepository.countByPostId(postId);
    }
    
    @Transactional
    public Comment update(Comment comment){
           validator.validateComment(comment);
           return commentRepository.save(comment);
    }

    @Transactional
    public void delete(Comment comment){
        commentRepository.delete(comment);
    }


     private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Usuário não encontrado."
                        )
                );
    }

    private Post findPostById(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Post não encontrado."
                        )
                );
    }
}
