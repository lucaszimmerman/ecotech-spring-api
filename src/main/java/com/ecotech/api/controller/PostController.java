package com.ecotech.api.controller;

import java.net.URI;
import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecotech.api.controller.common.GenericController;
import com.ecotech.api.controller.dto.CreatePostDTO;
import com.ecotech.api.controller.dto.PostResponseDTO;
import com.ecotech.api.controller.dto.UpdatePostDTO;
import com.ecotech.api.controller.mappers.PostMapper;
import com.ecotech.api.model.Post;
import com.ecotech.api.service.PostService;
import com.ecotech.api.service.PostInteractionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import static com.ecotech.api.config.OpenApiConfiguration.BEARER_AUTH;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts")
@SecurityRequirement(name = BEARER_AUTH)
public class PostController implements GenericController {

    private final PostService postService;
    private final PostMapper postMapper;
    private final PostInteractionService postInteractionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria uma publicacao")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publicacao criada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada ou arquivo invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> create(
            @ModelAttribute @Valid CreatePostDTO createPostDTO,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        Post post = postMapper.toEntity(createPostDTO);

        Post savedPost = postService.save(post, authenticatedUserId, file);

        URI location = generateLocationHeader(savedPost.getId());

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca uma publicacao por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicacao encontrada"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Publicacao nao encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<PostResponseDTO> getPostById(@PathVariable UUID id, Authentication authentication) {
        Post post = postService.findById(id);

        UUID userId = UUID.fromString(authentication.getName());
        var interactions = postInteractionService.summarize(List.of(id), userId);
        PostResponseDTO response = postMapper.toResponseDTO(post, interactions.get(id));

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista publicacoes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicacoes listadas"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Page<PostResponseDTO>> getAllPosts(
            @ParameterObject @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Page<PostResponseDTO> response = toResponsePage(postService.findAll(pageable), authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feed")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista publicacoes das pessoas seguidas",
            description = "Inclui apenas autores ativos seguidos pelo usuario autenticado. Sem follows, retorna pagina vazia. Ordem padrao: createdAt e id decrescentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed paginado com resumo de interacoes"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido")
    })
    public ResponseEntity<Page<PostResponseDTO>> getFeed(
            @ParameterObject @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(toResponsePage(postService.findFeed(userId, pageable), authentication));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista publicacoes de um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicacoes listadas"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Page<PostResponseDTO>> getPostsByUserId(
            @PathVariable UUID userId,
            @ParameterObject @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Page<PostResponseDTO> response = toResponsePage(postService.findByUserId(userId, pageable), authentication);

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @postAuthorization.isOwner(#id, authentication)")
    @Operation(summary = "Atualiza uma publicacao")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publicacao atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Publicacao nao encontrada"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada ou arquivo invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> update(
            @PathVariable UUID id,
            @ModelAttribute @Valid UpdatePostDTO updatePostDTO,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage,
            Authentication authentication) {

        Post post = postService.findById(id);

        postMapper.updateEntity(updatePostDTO, post);

        postService.update(post, file, removeImage);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @postAuthorization.isOwner(#id, authentication)")
    @Operation(summary = "Remove uma publicacao")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publicacao removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Publicacao nao encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication) {

        Post post = postService.findById(id);

        postService.delete(post);

        return ResponseEntity.noContent().build();
    }

    private Page<PostResponseDTO> toResponsePage(Page<Post> posts, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        var interactions = postInteractionService.summarize(
                posts.getContent().stream().map(Post::getId).toList(), userId);
        return posts.map(post -> postMapper.toResponseDTO(post, interactions.get(post.getId())));
    }

}
