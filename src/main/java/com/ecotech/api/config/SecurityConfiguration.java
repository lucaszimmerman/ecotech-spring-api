package com.ecotech.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.ecotech.api.security.SecurityExceptionHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

        private final AuthenticationProvider authenticationProvider;
        private final JwtAuthenticationConverter jwtAuthenticationConverter;
        private final SecurityExceptionHandler securityExceptionHandler;

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
                                                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(
                                                securityExceptionHandler)
                                                .accessDeniedHandler(
                                                 securityExceptionHandler))
                                .authenticationProvider(authenticationProvider)
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                .jwtAuthenticationConverter(
                                                jwtAuthenticationConverter)))
                                .build();
        }

}
