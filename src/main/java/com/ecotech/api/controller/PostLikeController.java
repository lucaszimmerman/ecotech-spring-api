package com.ecotech.api.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecotech.api.controller.dto.PostLikeStatusDTO;
import com.ecotech.api.controller.dto.UserSummaryDTO;
import com.ecotech.api.controller.mappers.UserMapper;
import com.ecotech.api.model.PostLike;
import com.ecotech.api.service.PostLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

import static com.ecotech.api.config.OpenApiConfiguration.BEARER_AUTH;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
@Tag(name = "Likes")
@SecurityRequirement(name = BEARER_AUTH)
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final UserMapper userMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Curte uma publicacao")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publicacao curtida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario ou publicacao nao encontrado"),
            @ApiResponse(responseCode = "409", description = "Publicacao ja curtida"),
            @ApiResponse(responseCode = "422", description = "Dados invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> like(
            @PathVariable UUID postId,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        postLikeService.like(authenticatedUserId, postId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove curtida de uma publicacao")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curtida removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> unlike(
            @PathVariable UUID postId,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        postLikeService.unlike(authenticatedUserId, postId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista usuarios que curtiram uma publicacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios listados"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Publicacao nao encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Page<UserSummaryDTO>> findByPostId(
            @PathVariable UUID postId,
            Pageable pageable) {
        Page<UserSummaryDTO> users = postLikeService
                .findByPostId(postId, pageable)
                .map(PostLike::getUser)
                .map(userMapper::toSummaryDTO);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca status de curtida de uma publicacao para o usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retornado"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario ou publicacao nao encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<PostLikeStatusDTO> status(
            @PathVariable UUID postId,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        long likeCount = postLikeService.countByPostId(postId);
        boolean likedByCurrentUser = postLikeService
                .likedByUser(authenticatedUserId, postId);

        PostLikeStatusDTO response = new PostLikeStatusDTO(
                likeCount,
                likedByCurrentUser);

        return ResponseEntity.ok(response);
    }
}
