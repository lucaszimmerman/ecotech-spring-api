package com.ecotech.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ecotech.api.support.TestJwtProperties;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class SocialFlowTest {

    @DynamicPropertySource
    static void registerJwtProperties(DynamicPropertyRegistry registry) {
        TestJwtProperties.register(registry);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private JsonNode register(String username) throws Exception {
        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username, "name", username,
                                "email", username + "@example.com", "password", "test-password"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    @Test
    void shouldCompleteDiscoveryFollowFeedAndInteractionsUsingRealJwtAndMappers() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        JsonNode alice = register("alice" + suffix);
        JsonNode bob = register("bob" + suffix);
        String aliceAuth = "Bearer " + alice.path("accessToken").asText();
        String bobAuth = "Bearer " + bob.path("accessToken").asText();
        String bobId = bob.path("id").asText();

        mockMvc.perform(get("/users/search").param("q", "bob" + suffix).header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(bobId))
                .andExpect(jsonPath("$.content[0].email").doesNotExist());
        mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(false));

        String location = mockMvc.perform(multipart("/posts")
                        .header(HttpHeaders.AUTHORIZATION, bobAuth).param("content", "A sustainability post."))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(location).isNotNull();
        String postId = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(get("/posts/feed").header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        mockMvc.perform(post("/users/{id}/follow", bobId).header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/posts/{id}/likes", postId).header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/posts/{id}/comments", postId).header(HttpHeaders.AUTHORIZATION, aliceAuth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Great idea!\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/posts/feed").header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(postId))
                .andExpect(jsonPath("$.content[0].likeCount").value(1))
                .andExpect(jsonPath("$.content[0].commentCount").value(1))
                .andExpect(jsonPath("$.content[0].likedByCurrentUser").value(true));
        mockMvc.perform(get("/posts/{id}", postId).header(HttpHeaders.AUTHORIZATION, bobAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.commentCount").value(1))
                .andExpect(jsonPath("$.likedByCurrentUser").value(false));

        mockMvc.perform(delete("/posts/{id}/likes", postId).header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/posts/{id}", postId).header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.likedByCurrentUser").value(false));
        mockMvc.perform(delete("/users/{id}/follow", bobId).header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/posts/feed").header(HttpHeaders.AUTHORIZATION, aliceAuth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }
}
