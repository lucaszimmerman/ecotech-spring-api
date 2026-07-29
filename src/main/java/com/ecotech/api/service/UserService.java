package com.ecotech.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public User save(User user) {
        normalizeForCreate(user);
        validator.validateForCreate(user);
        var password = user.getPassword();
        user.setPassword(encoder.encode(password));
        return repository.save(user);
    }

    @Transactional
    public void update(User user){
        normalizeForUpdate(user);
        validator.validateForUpdate(user);
        repository.save(user);
    }

    @Transactional
    public User findById(UUID id) {
    return repository.findById(id)
        .orElseThrow(() -> new RegistroNaoEncontradoException(
            "Usuário não encontrado."
        ));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void delete(User user){
        repository.delete(user);
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
