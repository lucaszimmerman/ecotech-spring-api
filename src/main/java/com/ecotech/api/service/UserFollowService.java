package com.ecotech.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.User;
import com.ecotech.api.model.UserFollow;
import com.ecotech.api.repository.UserFollowRepository;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.UserFollowValidator;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserFollowRepository repository;
    private final UserFollowValidator validator;
    private final UserRepository userRepository;

    @Transactional
    public void follow(UUID followerId, UUID followedId){
        
        User follower = findUserById(followerId);
        User followed = findUserById(followedId);

        UserFollow userFollow = new UserFollow();
        userFollow.setFollower(follower);
        userFollow.setFollowed(followed);

        validator.validateFollowing(userFollow);

        repository.save(userFollow);
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followedId){
        repository.findByFollowerIdAndFollowedId(
            followerId,
            followedId
        ).ifPresent(repository::delete);
    }

    @Transactional(readOnly = true)
    public Page<UserFollow> findFollowers(UUID userId, Pageable pageable){
        findUserById(userId);

        return repository.findByFollowedId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<UserFollow> findFollowing(UUID userId, Pageable pageable){
        findUserById(userId);

        return repository.findByFollowerId(userId, pageable);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() ->
                new RegistroNaoEncontradoException(
                    "Usuário não encontrado"
                )
            );
    }

    
}
