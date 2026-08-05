package com.ecotech.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecotech.api.controller.dto.UserSummaryDTO;
import com.ecotech.api.controller.mappers.UserMapper;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.PostLike;
import com.ecotech.api.model.User;
import com.ecotech.api.service.PostLikeService;
import com.ecotech.api.support.TestJwtProperties;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PostLikeControllerTest {

    @DynamicPropertySource
    static void registerJwtProperties(DynamicPropertyRegistry registry) {
        TestJwtProperties.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostLikeService postLikeService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private User createUser(UUID userId) {
        User user = new User();

        user.setId(userId);
        user.setUsername("lucas");
        user.setEmail("lucas@email.com");
        user.setPassword("123456");
        user.setName("Lucas Zimmerman");

        return user;
    }

    private Post createPost(UUID postId) {
        Post post = new Post();

        post.setId(postId);
        post.setContent("Conteudo valido do post.");
        post.setUser(createUser(UUID.randomUUID()));

        return post;
    }

    private PostLike createPostLike(User user, Post post) {
        PostLike postLike = new PostLike();

        postLike.setId(UUID.randomUUID());
        postLike.setUser(user);
        postLike.setPost(post);

        return postLike;
    }

    private UserSummaryDTO createUserSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getProfileImageUrl());
    }

    @Test
    void shouldLikePostWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        mockMvc.perform(post("/posts/{postId}/likes", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isNoContent());

        verify(postLikeService).like(userId, postId);
    }

    @Test
    void shouldReturnUnauthorizedWhenLikeRequestHasNoJwt() throws Exception {
        mockMvc.perform(post("/posts/{postId}/likes", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUnlikePostWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        mockMvc.perform(delete("/posts/{postId}/likes", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isNoContent());

        verify(postLikeService).unlike(userId, postId);
    }

    @Test
    void shouldReturnUnauthorizedWhenUnlikeRequestHasNoJwt() throws Exception {
        mockMvc.perform(delete("/posts/{postId}/likes", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFindPostLikesPaged() throws Exception {
        UUID authenticatedUserId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = createUser(UUID.randomUUID());
        Post post = createPost(postId);
        PostLike postLike = createPostLike(user, post);
        UserSummaryDTO userSummaryDTO = createUserSummaryDTO(user);
        Page<PostLike> page = new PageImpl<>(
                List.of(postLike),
                PageRequest.of(0, 10),
                1);

        when(postLikeService.findByPostId(eq(postId), any(Pageable.class)))
                .thenReturn(page);

        when(userMapper.toSummaryDTO(user))
                .thenReturn(userSummaryDTO);

        mockMvc.perform(get("/posts/{postId}/likes", postId)
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(jwt -> jwt.subject(authenticatedUserId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(user.getId().toString()))
                .andExpect(jsonPath("$.content[0].username").value(user.getUsername()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(postLikeService).findByPostId(eq(postId), any(Pageable.class));
        verify(userMapper).toSummaryDTO(user);
    }

    @Test
    void shouldGetPostLikeStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(postLikeService.countByPostId(postId))
                .thenReturn(5L);

        when(postLikeService.likedByUser(userId, postId))
                .thenReturn(true);

        mockMvc.perform(get("/posts/{postId}/likes/status", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(5))
                .andExpect(jsonPath("$.likedByCurrentUser").value(true));

        verify(postLikeService).countByPostId(postId);
        verify(postLikeService).likedByUser(userId, postId);
    }

    @Test
    void shouldReturnUnauthorizedWhenStatusRequestHasNoJwt() throws Exception {
        mockMvc.perform(get("/posts/{postId}/likes/status", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
