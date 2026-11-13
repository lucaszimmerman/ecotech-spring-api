package com.ecotech.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.AuthProvider;

public interface UserRepository extends JpaRepository<User, UUID>{

    @Query("""
            SELECT u FROM User u
            WHERE u.active = true
              AND (LOCATE(LOWER(:query), LOWER(u.username)) > 0
                   OR LOCATE(LOWER(:query), LOWER(u.name)) > 0)
            """)
    Page<User> searchActiveUsers(@Param("query") String query, Pageable pageable);

    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username,UUID id);
    Optional<User> findByAuthProviderAndProviderId(
        AuthProvider authProvider,
        String providerId
);
}
