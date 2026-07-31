package com.ecotech.api.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorization {
    
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    public boolean canAccess(
            UUID userId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return isAdmin(authentication)
                || isOwner(userId, authentication);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        ROLE_ADMIN.equals(authority.getAuthority())
                );
    }

    private boolean isOwner(
            UUID userId,
            Authentication authentication
    ) {
        return userId != null
                && userId.toString().equals(authentication.getName());
    }
}
