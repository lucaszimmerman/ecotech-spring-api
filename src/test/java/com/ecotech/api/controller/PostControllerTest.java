package com.ecotech.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecotech.api.controller.dto.CreatePostDTO;
import com.ecotech.api.controller.dto.PostResponseDTO;
import com.ecotech.api.controller.dto.UpdatePostDTO;
import com.ecotech.api.controller.dto.UserSummaryDTO;
import com.ecotech.api.controller.mappers.PostMapper;
import com.ecotech.api.model.Post;
import com.ecotech.api.model.User;
import com.ecotech.api.security.PostAuthorization;
import com.ecotech.api.service.PostService;
import com.ecotech.api.support.TestJwtProperties;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PostControllerTest {

    @DynamicPropertySource
    static void registerJwtProperties(DynamicPropertyRegistry registry) {
        TestJwtProperties.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private PostMapper postMapper;

    @MockitoBean
    private PostAuthorization postAuthorization;

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

    private Post createPost(UUID postId, UUID userId) {
        Post post = new Post();

        post.setId(postId);
        post.setContent("Conteudo valido do post.");
        post.setImageUrl("https://example.com/image.png");
        post.setUser(createUser(userId));
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        return post;
    }

    private PostResponseDTO createResponseDTO(Post post) {
        User user = post.getUser();

        return new PostResponseDTO(
                post.getId(),
                post.getContent(),
                post.getImageUrl(),
                new UserSummaryDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getName(),
                        user.getProfileImageUrl()),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    @Test
    void shouldCreatePostWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = createPost(postId, userId);

        when(postMapper.toEntity(any(CreatePostDTO.class)))
                .thenReturn(post);

        when(postService.save(post, userId))
                .thenReturn(post);

        mockMvc.perform(post("/posts")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Conteudo valido do post.",
                                  "imageUrl": "https://example.com/image.png"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/posts/" + postId));

        verify(postMapper).toEntity(any(CreatePostDTO.class));
        verify(postService).save(post, userId);
    }

    @Test
    void shouldReturnUnauthorizedWhenRequestHasNoJwt() throws Exception {
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Conteudo valido do post."
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnprocessableEntityWhenContentIsInvalid() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/posts")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "   "
                                }
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void shouldGetPostById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = createPost(postId, userId);
        PostResponseDTO responseDTO = createResponseDTO(post);

        when(postService.findById(postId))
                .thenReturn(post);

        when(postMapper.toResponseDTO(post))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/posts/{id}", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.content").value(post.getContent()))
                .andExpect(jsonPath("$.user.id").value(userId.toString()));

        verify(postService).findById(postId);
        verify(postMapper).toResponseDTO(post);
    }

    @Test
    void shouldGetPostsPaged() throws Exception {
        UUID userId = UUID.randomUUID();
        Post post = createPost(UUID.randomUUID(), userId);
        Page<Post> page = new PageImpl<>(
                List.of(post),
                PageRequest.of(0, 10),
                1);

        when(postService.findAll(any(Pageable.class)))
                .thenReturn(page);

        when(postMapper.toResponseDTO(post))
                .thenReturn(createResponseDTO(post));

        mockMvc.perform(get("/posts")
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(post.getId().toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(postService).findAll(any(Pageable.class));
        verify(postMapper).toResponseDTO(post);
    }

    @Test
    void shouldGetPostsByUserIdPaged() throws Exception {
        UUID userId = UUID.randomUUID();
        Post post = createPost(UUID.randomUUID(), userId);
        Page<Post> page = new PageImpl<>(
                List.of(post),
                PageRequest.of(0, 10),
                1);

        when(postService.findByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(page);

        when(postMapper.toResponseDTO(post))
                .thenReturn(createResponseDTO(post));

        mockMvc.perform(get("/posts/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].user.id").value(userId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(postService).findByUserId(eq(userId), any(Pageable.class));
        verify(postMapper).toResponseDTO(post);
    }

    @Test
    void shouldAllowOwnerToUpdatePost() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = createPost(postId, userId);

        when(postAuthorization.isOwner(eq(postId), any()))
                .thenReturn(true);

        when(postService.findById(postId))
                .thenReturn(post);

        mockMvc.perform(put("/posts/{id}", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Conteudo atualizado.",
                                  "imageUrl": "https://example.com/updated.png"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(postAuthorization).isOwner(eq(postId), any());
        verify(postService).findById(postId);
        verify(postMapper).updateEntity(any(UpdatePostDTO.class), eq(post));
        verify(postService).update(post);
    }

    @Test
    void shouldForbidAnotherUserToUpdatePost() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(postAuthorization.isOwner(eq(postId), any()))
                .thenReturn(false);

        mockMvc.perform(put("/posts/{id}", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Conteudo atualizado."
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(postAuthorization).isOwner(eq(postId), any());
    }

    @Test
    void shouldAllowAdminToUpdatePost() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = createPost(postId, UUID.randomUUID());

        when(postService.findById(postId))
                .thenReturn(post);

        mockMvc.perform(put("/posts/{id}", postId)
                        .with(jwt()
                                .jwt(jwt -> jwt.subject(adminId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Conteudo atualizado."
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(postService).findById(postId);
        verify(postMapper).updateEntity(any(UpdatePostDTO.class), eq(post));
        verify(postService).update(post);
    }

    @Test
    void shouldAllowOwnerToDeletePost() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = createPost(postId, userId);

        when(postAuthorization.isOwner(eq(postId), any()))
                .thenReturn(true);

        when(postService.findById(postId))
                .thenReturn(post);

        mockMvc.perform(delete("/posts/{id}", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isNoContent());

        verify(postAuthorization).isOwner(eq(postId), any());
        verify(postService).findById(postId);
        verify(postService).delete(post);
    }

    @Test
    void shouldForbidAnotherUserToDeletePost() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(postAuthorization.isOwner(eq(postId), any()))
                .thenReturn(false);

        mockMvc.perform(delete("/posts/{id}", postId)
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isForbidden());

        verify(postAuthorization).isOwner(eq(postId), any());
    }

    @Test
    void shouldAllowAdminToDeletePost() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post post = createPost(postId, UUID.randomUUID());

        when(postService.findById(postId))
                .thenReturn(post);

        mockMvc.perform(delete("/posts/{id}", postId)
                        .with(jwt()
                                .jwt(jwt -> jwt.subject(adminId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        verify(postService).findById(postId);
        verify(postService).delete(post);
    }
}
