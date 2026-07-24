package com.ecotech.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecotech.api.model.User;

public interface UserRepository extends JpaRepository<User, UUID>{

    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
