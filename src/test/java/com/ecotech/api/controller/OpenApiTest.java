package com.ecotech.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.ecotech.api.support.TestJwtProperties;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class OpenApiTest {

    @DynamicPropertySource
    static void registerJwtProperties(DynamicPropertyRegistry registry) {
        TestJwtProperties.register(registry);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldDocumentAuthenticationAndSocialEndpointsWithPublicAndPrivateContracts() throws Exception {
        String response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode document = objectMapper.readTree(response);
        JsonNode paths = document.path("paths");

        assertThat(paths.has("/auth/register")).isTrue();
        assertThat(paths.has("/auth/login")).isTrue();
        assertThat(paths.has("/auth/resend-verification")).isTrue();
        assertThat(paths.has("/users/me")).isTrue();
        assertThat(paths.has("/users/{id}/profile")).isTrue();
        assertThat(paths.has("/users/search")).isTrue();
        assertThat(paths.has("/posts/feed")).isTrue();
        assertThat(paths.has("/posts/{postId}/comments")).isTrue();
        assertThat(paths.has("/posts/{postId}/likes")).isTrue();
        assertThat(paths.path("/posts").path("post").path("requestBody").path("content")
                .has("multipart/form-data")).isTrue();
        assertThat(document.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(paths.path("/auth/login").path("post").path("security").isMissingNode()).isTrue();
        assertThat(paths.path("/posts/feed").path("get").path("security").toString()).contains("bearerAuth");
        assertThat(paths.path("/users/search").path("get").path("parameters").toString())
                .contains("\"q\"", "\"page\"", "\"size\"", "\"sort\"");

        JsonNode schemas = document.at("/components/schemas");
        assertThat(schemas.path("UserResponseDTO").path("properties").has("emailVerified")).isTrue();
        assertThat(schemas.path("UserPublicProfileDTO").path("properties").has("email")).isFalse();
        assertThat(schemas.path("PostResponseDTO").path("properties").has("likeCount")).isTrue();
        assertThat(schemas.path("PostResponseDTO").path("properties").has("commentCount")).isTrue();
        assertThat(schemas.path("PostResponseDTO").path("properties").has("likedByCurrentUser")).isTrue();
    }

    @Test
    void shouldKeepSwaggerAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
