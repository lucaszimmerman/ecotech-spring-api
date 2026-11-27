package com.ecotech.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecotech.api.config.FrontendProperties;
import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.exceptions.OperacaoNaoPermitidaException;
import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.UserRole;
import com.ecotech.api.service.GoogleAuthenticationService;
import com.ecotech.api.service.GoogleAuthenticationService.GoogleLoginResult;
import com.ecotech.api.service.JwtService;

class GoogleOAuth2SuccessHandlerTest {

    private final GoogleAuthenticationService googleAuthenticationService =
            org.mockito.Mockito.mock(GoogleAuthenticationService.class);

    private final JwtService jwtService =
            org.mockito.Mockito.mock(JwtService.class);

    private final JwtProperties jwtProperties =
            org.mockito.Mockito.mock(JwtProperties.class);

    private final GoogleLoginCodeStore codeStore =
            new GoogleLoginCodeStore();

    private final GoogleOAuth2SuccessHandler handler =
            new GoogleOAuth2SuccessHandler(
                    googleAuthenticationService,
                    jwtService,
                    jwtProperties,
                    codeStore,
                    new FrontendProperties("https://ecotech.example/")
            );

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("lucas");
        user.setName("Lucas Zimmerman");
        user.setRole(UserRole.USER);
        user.setActive(true);
        return user;
    }

    private Authentication authentication(OidcUser oidcUser) {
        Authentication authentication =
                org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        return authentication;
    }

    @Test
    void shouldRedirectToFrontendWithOneTimeCodeInsteadOfJwt() throws Exception {
        OidcUser oidcUser = org.mockito.Mockito.mock(OidcUser.class);
        User user = user();

        when(googleAuthenticationService.authenticateForLogin(oidcUser))
                .thenReturn(new GoogleLoginResult(user, true));
        when(jwtService.generateToken(any(UserPrincipal.class)))
                .thenReturn("access-token");
        when(jwtProperties.expiration()).thenReturn(3600L);

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(), response, authentication(oidcUser));

        String location = response.getRedirectedUrl();
        assertThat(location)
                .startsWith("https://ecotech.example/auth/google?code=")
                .contains("novo=1")
                .doesNotContain("access-token");

        String code = UriComponentsBuilder.fromUriString(location)
                .build().getQueryParams().getFirst("code");
        LoginResponseDTO exchanged = codeStore.consume(code).orElseThrow();
        assertThat(exchanged.id()).isEqualTo(user.getId());
        assertThat(exchanged.accessToken()).isEqualTo("access-token");
        assertThat(exchanged.expiresIn()).isEqualTo(3600L);
        assertThat(codeStore.consume(code)).isEmpty();
    }

    @Test
    void shouldNotMarkExistingUsersAsNew() throws Exception {
        OidcUser oidcUser = org.mockito.Mockito.mock(OidcUser.class);

        when(googleAuthenticationService.authenticateForLogin(oidcUser))
                .thenReturn(new GoogleLoginResult(user(), false));
        when(jwtService.generateToken(any(UserPrincipal.class)))
                .thenReturn("access-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(), response, authentication(oidcUser));

        assertThat(response.getRedirectedUrl()).doesNotContain("novo=");
    }

    @Test
    void shouldRedirectWithMessageWhenGoogleUserIsRejected() throws Exception {
        OidcUser oidcUser = org.mockito.Mockito.mock(OidcUser.class);

        when(googleAuthenticationService.authenticateForLogin(oidcUser))
                .thenThrow(new OperacaoNaoPermitidaException(
                        "Ja existe uma conta associada a este email."));

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(), response, authentication(oidcUser));

        String erro = UriComponentsBuilder.fromUriString(response.getRedirectedUrl())
                .build().getQueryParams().getFirst("erro");
        assertThat(response.getRedirectedUrl())
                .startsWith("https://ecotech.example/auth/google?erro=");
        assertThat(java.net.URLDecoder.decode(erro, java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo("Ja existe uma conta associada a este email.");
    }
}
