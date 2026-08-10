package com.ecotech.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecotech.api.controller.dto.ChangePasswordDTO;
import com.ecotech.api.exceptions.CampoInvalidoException;
import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.User;
import com.ecotech.api.repository.UserRepository;
import com.ecotech.api.validator.UserValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserValidator validator;
    private final PasswordEncoder encoder;
    private final ImageStorageService imageStorageService;

    @Transactional
    public User save(User user) {
        normalizeForCreate(user);
        validator.validateForCreate(user);
        var password = user.getPassword();
        user.setPassword(encoder.encode(password));
        return repository.save(user);
    }

    @Transactional
    public void update(User user) {
        normalizeForUpdate(user);
        validator.validateForUpdate(user);
        repository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void delete(User user) {
        repository.delete(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordDTO dto) {
        User user = findById(userId);

        if (!encoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new CampoInvalidoException(
                    "currentPassword",
                    "A senha atual está incorreta.");
        }

        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new CampoInvalidoException(
                    "confirmPassword",
                    "A confirmação da nova senha não confere.");
        }

        if (encoder.matches(dto.newPassword(), user.getPassword())) {
            throw new CampoInvalidoException(
                    "newPassword",
                    "A nova senha deve ser diferente da senha atual.");
        }

        user.setPassword(encoder.encode(dto.newPassword()));
        repository.save(user);
    }

    @Transactional
    public void updateProfileImage(
            UUID userId,
            MultipartFile file) {
        User user = findById(userId);

        String oldImageKey = user.getProfileImageUrl();

        String newImageKey = imageStorageService.upload(
                file,
                "users/" + userId + "/profile");

        user.setProfileImageUrl(newImageKey);

        repository.save(user);

        if (oldImageKey != null && !oldImageKey.isBlank()) {
            imageStorageService.delete(oldImageKey);
        }
    }

    @Transactional
    public void updateCoverImage(
            UUID userId,
            MultipartFile file) {
        User user = findById(userId);

        String oldImageKey = user.getCoverImageUrl();

        String newImageKey = imageStorageService.upload(
                file,
                "users/" + userId + "/cover");

        user.setCoverImageUrl(newImageKey);

        repository.save(user);

        if (oldImageKey != null && !oldImageKey.isBlank()) {
            imageStorageService.delete(oldImageKey);
        }
    }

    private void normalizeForCreate(User user) {
        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setName(user.getName().trim());
    }

    private void normalizeForUpdate(User user) {
        user.setUsername(user.getUsername().trim());
        user.setName(user.getName().trim());
    }
}
