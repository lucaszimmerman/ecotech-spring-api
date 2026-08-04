package com.ecotech.api.validator;

import org.springframework.stereotype.Component;

import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.RegistroDuplicadoException;
import com.ecotech.api.model.UserFollow;
import com.ecotech.api.repository.UserFollowRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserFollowValidator {

    private final UserFollowRepository repository;

    public void validateFollowing(UserFollow userFollow) {
        validateRequiredUsers(userFollow);
        validateCannotFollowYourself(userFollow);
        validateDuplicateFollow(userFollow);
    }

    private void validateRequiredUsers(UserFollow userFollow) {
        
        if (userFollow == null) {
            throw new CampoInvalidoException(
                    "userFollow",
                    "O relacionamento de usuários é obrigatório.");
        }

        if (userFollow.getFollower() == null){
            throw new CampoInvalidoException(
                    "follower",
                    "O usuário seguidor é obrigatório."
            );
        }

        if (userFollow.getFollowed() == null){
            throw new CampoInvalidoException(
                "followed",
                "O usuário seguido é obrigatório."
            );
        }
    }

    private void validateCannotFollowYourself(UserFollow userFollow){
        if (userFollow.getFollower().getId()
            .equals(userFollow.getFollowed().getId())) {
            throw new CampoInvalidoException(
                "followed",
                "O usuário não pode seguir a si mesmo."
            );
        }
    }

    private void validateDuplicateFollow(UserFollow userFollow) {
        boolean alreadyFollowing = repository.existsByFollowerIdAndFollowedId(
            userFollow.getFollower().getId(),
            userFollow.getFollowed().getId()
        );

        if (alreadyFollowing) {
            throw new RegistroDuplicadoException(
                "O usuário já está seguindo este usuário."
            );
        }
    }
}
