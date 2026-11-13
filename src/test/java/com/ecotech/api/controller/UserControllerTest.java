package com.ecotech.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.ecotech.api.controller.dto.ChangePasswordDTO;
import com.ecotech.api.exceptions.RegistroNaoEncontradoException;
import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.UserRole;
import com.ecotech.api.service.ImageStorageService;
import com.ecotech.api.service.UserService;
import com.ecotech.api.support.TestJwtProperties;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {

    @DynamicPropertySource
    static void registerJwtProperties(DynamicPropertyRegistry registry) {
        TestJwtProperties.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @Test
    void shouldReturnAnotherUsersPublicProfileWithoutPrivateFields() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("ecouser");
        user.setName("Eco User");
        user.setProfileImageUrl("users/profile.png");
        user.setCoverImageUrl("users/cover.png");
        user.setCity("Maringa");
        user.setWebsite("https://example.com");
        user.setBio("Sustentabilidade no dia a dia.");
        user.setEmail("private@example.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.USER);
        user.setEmailVerified(true);
        user.setProviderId("private-provider-id");

        when(userService.findById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{id}/profile", userId)
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("ecouser"))
                .andExpect(jsonPath("$.name").value("Eco User"))
                .andExpect(jsonPath("$.profileImageUrl").value("users/profile.png"))
                .andExpect(jsonPath("$.coverImageUrl").value("users/cover.png"))
                .andExpect(jsonPath("$.city").value("Maringa"))
                .andExpect(jsonPath("$.website").value("https://example.com"))
                .andExpect(jsonPath("$.bio").value("Sustentabilidade no dia a dia."))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.active").doesNotExist())
                .andExpect(jsonPath("$.emailVerified").doesNotExist())
                .andExpect(jsonPath("$.authProvider").doesNotExist())
                .andExpect(jsonPath("$.providerId").doesNotExist());

        verify(userService).findById(userId);
    }

    @Test
    void shouldReturnUnauthorizedWhenPublicProfileRequestHasNoJwt() throws Exception {
        mockMvc.perform(get("/users/{id}/profile", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturnNotFoundWhenPublicProfileDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId))
                .thenThrow(new RegistroNaoEncontradoException("Usuario nao encontrado."));

        mockMvc.perform(get("/users/{id}/profile", userId)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnBadRequestWhenPublicProfileIdIsInvalid() throws Exception {
        mockMvc.perform(get("/users/invalid-id/profile")
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(userService);
    }

    @Test
    void shouldKeepAnotherUsersPrivateDetailsRestricted() throws Exception {
        mockMvc.perform(get("/users/{id}", UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldChangePasswordWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/users/me/password")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "old-password",
                                  "newPassword": "new-password",
                                  "confirmPassword": "new-password"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq(userId), any(ChangePasswordDTO.class));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenNewPasswordIsTooShort() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/users/me/password")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "old-password",
                                  "newPassword": "12345",
                                  "confirmPassword": "12345"
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void shouldReturnUnauthorizedWhenChangePasswordRequestHasNoJwt() throws Exception {
        mockMvc.perform(patch("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "old-password",
                                  "newPassword": "new-password",
                                  "confirmPassword": "new-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
