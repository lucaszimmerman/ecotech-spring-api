package com.ecotech.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.exceptions.RegistroDuplicadoException;
import com.ecotech.api.model.enums.UserRole;
import com.ecotech.api.service.AuthenticationService;
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
}
