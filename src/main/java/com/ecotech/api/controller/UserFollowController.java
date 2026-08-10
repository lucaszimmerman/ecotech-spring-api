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

import com.ecotech.api.controller.dto.UserSummaryDTO;
import com.ecotech.api.controller.mappers.UserMapper;
import com.ecotech.api.model.UserFollow;
import com.ecotech.api.service.UserFollowService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

import static com.ecotech.api.config.OpenApiConfiguration.BEARER_AUTH;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Follows")
@SecurityRequirement(name = BEARER_AUTH)
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final UserMapper userMapper;

    @PostMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Segue um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario seguido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "409", description = "Usuario ja seguido"),
            @ApiResponse(responseCode = "422", description = "Operacao invalida"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> follow(
        @PathVariable UUID id,
        Authentication authentication
    )
    {
        UUID authenticatedUserId =
             UUID.fromString(authentication.getName());

        userFollowService.follow(authenticatedUserId, id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Deixa de seguir um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario deixado de seguir com sucesso"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> unfollow(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID authenticatedUserId = UUID.fromString(authentication.getName());
        userFollowService.unfollow(authenticatedUserId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/followers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista seguidores de um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguidores listados"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Page<UserSummaryDTO>> findFollowers(
            @PathVariable UUID id,
            Pageable pageable) {
        Page<UserSummaryDTO> followers = userFollowService
                .findFollowers(id, pageable)
                .map(UserFollow::getFollower)
                .map(userMapper::toSummaryDTO);

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{id}/following")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista usuarios seguidos por um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios seguidos listados"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Page<UserSummaryDTO>> findFollowing(
            @PathVariable UUID id,
            Pageable pageable) {
        Page<UserSummaryDTO> following = userFollowService
                .findFollowing(id, pageable)
                .map(UserFollow::getFollowed)
                .map(userMapper::toSummaryDTO);

        return ResponseEntity.ok(following);
    }
}
