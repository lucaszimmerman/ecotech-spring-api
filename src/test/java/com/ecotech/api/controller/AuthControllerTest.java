package com.ecotech.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecotech.api.controller.dto.CreateUserDTO;
import com.ecotech.api.controller.dto.auth.ForgotPasswordDTO;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.controller.dto.auth.ResetPasswordDTO;
import com.ecotech.api.exceptions.RegistroDuplicadoException;
import com.ecotech.api.model.enums.UserRole;
import com.ecotech.api.service.AuthenticationService;
import com.ecotech.api.service.EmailVerificationService;
import com.ecotech.api.service.PasswordRecoveryService;
import com.ecotech.api.support.TestJwtProperties;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerTest {

    @DynamicPropertySource
    static void registerJwtProperties(DynamicPropertyRegistry registry) {
        TestJwtProperties.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private PasswordRecoveryService passwordRecoveryService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRegisterUserWithoutJwt() throws Exception {
        UUID userId = UUID.randomUUID();
        LoginResponseDTO response = new LoginResponseDTO(
                userId,
                "lucas",
                "Lucas Zimmerman",
                UserRole.USER,
                "access-token",
                "Bearer",
                3600L);

        when(authenticationService.register(any(CreateUserDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "lucas",
                                  "email": "lucas@email.com",
                                  "password": "123456",
                                  "name": "Lucas Zimmerman"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("lucas"))
                .andExpect(jsonPath("$.name").value("Lucas Zimmerman"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(authenticationService).register(any(CreateUserDTO.class));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenRegisterPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "lu",
                                  "email": "email-invalido",
                                  "password": "123",
                                  "name": ""
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldReturnConflictWhenRegisterUserAlreadyExists() throws Exception {
        when(authenticationService.register(any(CreateUserDTO.class)))
                .thenThrow(new RegistroDuplicadoException(
                        "O nome de usuario ja esta em uso."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "lucas",
                                  "email": "lucas@email.com",
                                  "password": "123456",
                                  "name": "Lucas Zimmerman"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(authenticationService).register(any(CreateUserDTO.class));
    }

    @Test
    void shouldVerifyEmailWithoutJwt() throws Exception {
        String token = "verification-token";

        doNothing().when(emailVerificationService).verifyEmail(token);

        mockMvc.perform(get("/auth/verify-email")
                        .param("token", token))
                .andExpect(status().isOk());

        verify(emailVerificationService).verifyEmail(token);
    }

    @Test
    void shouldRejectResendVerificationWithoutJwt() throws Exception {
        mockMvc.perform(post("/auth/resend-verification"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void shouldResendVerificationWithJwt() throws Exception {
        UUID userId = UUID.randomUUID();

        doNothing().when(emailVerificationService).resendVerification(userId);

        mockMvc.perform(post("/auth/resend-verification")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isNoContent());

        verify(emailVerificationService).resendVerification(userId);
    }

    @Test
    void shouldRequestPasswordResetWithoutJwt() throws Exception {
        doNothing().when(passwordRecoveryService).requestPasswordReset("lucas@email.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "lucas@email.com"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(passwordRecoveryService).requestPasswordReset("lucas@email.com");
    }

    @Test
    void shouldReturnUnprocessableEntityWhenForgotPasswordPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "email-invalido"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));

        verifyNoInteractions(passwordRecoveryService);
    }

    @Test
    void shouldResetPasswordWithoutJwt() throws Exception {
        doNothing().when(passwordRecoveryService).resetPassword(any(ResetPasswordDTO.class));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "reset-token",
                                  "newPassword": "123456",
                                  "confirmPassword": "123456"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(passwordRecoveryService).resetPassword(any(ResetPasswordDTO.class));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenResetPasswordPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "",
                                  "newPassword": "123",
                                  "confirmPassword": ""
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));

        verifyNoInteractions(passwordRecoveryService);
    }
}
