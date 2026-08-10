package com.ecotech.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.exceptions.OperacaoNaoPermitidaException;
import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.UserRole;
import com.ecotech.api.service.GoogleAuthenticationService;
import com.ecotech.api.service.JwtService;

import tools.jackson.databind.ObjectMapper;

class GoogleOAuth2SuccessHandlerTest {

    private final GoogleAuthenticationService googleAuthenticationService =
            org.mockito.Mockito.mock(GoogleAuthenticationService.class);

    private final JwtService jwtService =
            org.mockito.Mockito.mock(JwtService.class);

    private final JwtProperties jwtProperties =
            org.mockito.Mockito.mock(JwtProperties.class);

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private final GoogleOAuth2FailureHandler googleOAuth2FailureHandler =
            new GoogleOAuth2FailureHandler(objectMapper);

    private final GoogleOAuth2SuccessHandler handler =
            new GoogleOAuth2SuccessHandler(
                    googleAuthenticationService,
                    jwtService,
                    jwtProperties,
                    objectMapper,
                    googleOAuth2FailureHandler
            );

    @Test
    void shouldReturnJwtResponseWhenGoogleLoginSucceeds() throws Exception {
        OidcUser oidcUser =
                org.mockito.Mockito.mock(OidcUser.class);
        Authentication authentication =
                org.mockito.Mockito.mock(Authentication.class);
        UUID userId = UUID.randomUUID();
        User user = new User();

        user.setId(userId);
        user.setUsername("lucas");
        user.setName("Lucas Zimmerman");
        user.setRole(UserRole.USER);
        user.setActive(true);

        when(authentication.getPrincipal())
                .thenReturn(oidcUser);
        when(googleAuthenticationService.authenticate(oidcUser))
                .thenReturn(user);
        when(jwtService.generateToken(any(UserPrincipal.class)))
                .thenReturn("access-token");
        when(jwtProperties.expiration())
                .thenReturn(3600L);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                response,
                authentication
        );

        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentType())
                .startsWith("application/json");
        assertThat(response.getContentAsString())
                .contains("\"id\":\"" + userId + "\"")
                .contains("\"username\":\"lucas\"")
                .contains("\"accessToken\":\"access-token\"")
                .contains("\"tokenType\":\"Bearer\"")
                .contains("\"expiresIn\":3600");

        verify(jwtService).generateToken(any(UserPrincipal.class));
    }

    @Test
    void shouldReturnBadRequestWhenGoogleUserIsRejected() throws Exception {
        OidcUser oidcUser =
                org.mockito.Mockito.mock(OidcUser.class);
        Authentication authentication =
                org.mockito.Mockito.mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(oidcUser);
        when(googleAuthenticationService.authenticate(oidcUser))
                .thenThrow(new OperacaoNaoPermitidaException(
                        "Ja existe uma conta associada a este email."));

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                response,
                authentication
        );

        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("\"status\":400")
                .contains("\"mensagem\":\"Ja existe uma conta associada a este email.\"");
    }
}
