package com.ecotech.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.ecotech.api.security.GoogleOAuth2FailureHandler;
import com.ecotech.api.security.GoogleOAuth2SuccessHandler;
import com.ecotech.api.security.SecurityExceptionHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

        private final AuthenticationProvider authenticationProvider;
        private final JwtAuthenticationConverter jwtAuthenticationConverter;
        private final SecurityExceptionHandler securityExceptionHandler;
        private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;
        private final GoogleOAuth2FailureHandler googleOAuth2FailureHandler;

        @Bean
        @Order(1)
        SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {

                return http
                                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                                .anyRequest().permitAll())
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(
                                                                securityExceptionHandler)
                                                .accessDeniedHandler(
                                                                securityExceptionHandler))
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(
                                                                googleOAuth2SuccessHandler)
                                                .failureHandler(
                                                                googleOAuth2FailureHandler))
                                .build();
        }

        @Bean
        @Order(2)
        SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {

                return http
                                // APIs REST com JWT nao utilizam autenticacao baseada em sessao/cookies,
                                // portanto a protecao CSRF tradicional nao e necessaria.
                                .csrf(csrf -> csrf.disable())
                                // Define que a API e STATELESS.
                                // O servidor nao armazenara sessao do usuario.
                                // Cada requisicao devera enviar um JWT valido.
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/auth/verify-email").permitAll()
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
