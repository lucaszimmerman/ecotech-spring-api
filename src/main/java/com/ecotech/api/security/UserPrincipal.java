package com.ecotech.api.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.UserRole;

/**
 * Representação do usuário autenticado dentro do Spring Security.
 *
 * Enquanto a entidade User representa o usuário no banco de dados,
 * o UserPrincipal representa esse mesmo usuário durante o processo
 * de autenticação e autorização.
 *
 * Em vez de usar a implementação pronta do Spring (User),
 * criamos nossa própria classe para manter informações extras,
 * como o UUID do usuário, facilitando futuras funcionalidades
 * como criação de posts, comentários, likes e geração de JWT.
 */
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final String name;
    private final UserRole role;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.name = user.getName();
        this.role = user.getRole();
        this.active = user.isActive();

        this.authorities = List.of(
            new SimpleGrantedAuthority(
                "ROLE_" + user.getRole().name()
            )
        );
    }

    public UUID getId() {
        return id;
    }

     public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}