package com.ecotech.api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.controller.dto.CreateUserDTO;
import com.ecotech.api.controller.dto.auth.LoginRequestDTO;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.controller.mappers.UserMapper;
import com.ecotech.api.model.User;
import com.ecotech.api.security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

        private final EmailVerificationService emailVerificationService;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final JwtProperties jwtProperties;
        private final UserService userService;
        private final UserMapper userMapper;

        public LoginResponseDTO login(LoginRequestDTO dto) {
                log.debug("Iniciando autenticacao local.");

                Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                                dto.username(),
                                dto.password());

                Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

                UserPrincipal principal = (UserPrincipal) authenticationResponse.getPrincipal();

                String accessToken = jwtService.generateToken(principal);

                log.info("Autenticacao local concluida. userId={}", principal.getId());

                return toLoginResponse(principal, accessToken);
        }

        @Transactional
        public LoginResponseDTO register(CreateUserDTO dto) {
                log.debug("Iniciando cadastro de usuario local.");

                User user = userMapper.toEntity(dto);

                User savedUser = userService.save(user);

                emailVerificationService.sendVerification(savedUser);

                UserPrincipal principal = new UserPrincipal(savedUser);

                String accessToken = jwtService.generateToken(principal);

                log.info("Cadastro local concluido. userId={}", principal.getId());

                return toLoginResponse(principal, accessToken);
        }

        private LoginResponseDTO toLoginResponse(UserPrincipal principal, String accessToken) {
                return new LoginResponseDTO(
                                principal.getId(),
                                principal.getUsername(),
                                principal.getName(),
                                principal.getRole(),
                                accessToken,
                                "Bearer",
                                jwtProperties.expiration());
        }
}
