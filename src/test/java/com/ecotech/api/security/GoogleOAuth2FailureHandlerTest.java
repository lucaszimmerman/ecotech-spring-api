package com.ecotech.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import com.ecotech.api.config.FrontendProperties;

class GoogleOAuth2FailureHandlerTest {

    private final GoogleOAuth2FailureHandler handler =
            new GoogleOAuth2FailureHandler(
                    new FrontendProperties("http://localhost:4200"));

    @Test
    void shouldRedirectToFrontendWithGenericError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                new MockHttpServletRequest(),
                response,
                new OAuth2AuthenticationException(new OAuth2Error("access_denied")));

        assertThat(response.getRedirectedUrl())
                .startsWith("http://localhost:4200/auth/google?erro=")
                .doesNotContain("access_denied");
    }
}
