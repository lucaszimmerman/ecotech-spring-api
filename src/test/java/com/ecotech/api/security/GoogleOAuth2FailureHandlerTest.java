package com.ecotech.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import tools.jackson.databind.ObjectMapper;

class GoogleOAuth2FailureHandlerTest {

    private final GoogleOAuth2FailureHandler handler =
            new GoogleOAuth2FailureHandler(new ObjectMapper());

    @Test
    void shouldReturnJsonErrorWhenOAuthAuthenticationFails() throws Exception {
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("invalid state")
        );

        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType())
                .startsWith("application/json");
        assertThat(response.getCharacterEncoding())
                .isEqualTo("UTF-8");
        assertThat(response.getContentAsString())
                .contains("\"status\":401")
                .contains("\"mensagem\":\"Nao foi possivel autenticar com Google.\"")
                .contains("\"erros\":[]");
    }

    @Test
    void shouldWriteBusinessRuleError() throws Exception {
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        handler.writeError(
                response,
                HttpStatus.BAD_REQUEST,
                "Conta Google invalida."
        );

        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString())
                .contains("\"status\":400")
                .contains("\"mensagem\":\"Conta Google invalida.\"");
    }
}
