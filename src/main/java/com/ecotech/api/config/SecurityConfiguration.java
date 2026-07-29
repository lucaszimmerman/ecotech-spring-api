package com.ecotech.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {
    
    private final AuthenticationProvider authenticationProvider;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                // APIs REST com JWT não utilizam autenticação baseada em sessão/cookies,
                // portanto a proteção CSRF tradicional não é necessária.
                .csrf(csrf -> csrf.disable())
                // Define que a API é STATELESS.
                // O servidor NÃO armazenará sessão do usuário.
                // Cada requisição deverá enviar um JWT válido.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider)
                .build();
    }

}
