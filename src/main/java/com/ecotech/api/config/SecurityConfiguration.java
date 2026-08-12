package com.ecotech.api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ecotech.api.security.SecurityExceptionHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

        private static final String[] SWAGGER_ENDPOINTS = {
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
        };

        private static final String[] ACTUATOR_PUBLIC_ENDPOINTS = {
                        "/actuator/health",
                        "/actuator/health/**"
        };

        private static final String[] PUBLIC_POST_ENDPOINTS = {
                        "/auth/register",
                        "/auth/login",
                        "/auth/forgot-password",
                        "/auth/reset-password"
        };

        private static final String[] PUBLIC_GET_ENDPOINTS = {
                        "/auth/verify-email"
        };

        private final AuthenticationProvider authenticationProvider;
        private final JwtAuthenticationConverter jwtAuthenticationConverter;
        private final SecurityExceptionHandler securityExceptionHandler;
        private final CorsProperties corsProperties;

        @Bean
        SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {

                return http
                                // APIs REST com JWT nao utilizam autenticacao baseada em sessao/cookies,
                                // portanto a protecao CSRF tradicional nao e necessaria.
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                // Define que a API e STATELESS.
                                // O servidor nao armazenara sessao do usuario.
                                // Cada requisicao devera enviar um JWT valido.
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                                                .requestMatchers(ACTUATOR_PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                                                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
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

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(corsProperties.allowedOrigins());
                configuration.setAllowedMethods(
                                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(
                                List.of("Authorization", "Content-Type", "Accept"));
                configuration.setExposedHeaders(List.of("Location"));
                configuration.setAllowCredentials(false);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
