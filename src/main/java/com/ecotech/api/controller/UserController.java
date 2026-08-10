package com.ecotech.api.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecotech.api.controller.common.GenericController;
import com.ecotech.api.controller.dto.ChangePasswordDTO;
import com.ecotech.api.controller.dto.CreateUserDTO;
import com.ecotech.api.controller.dto.UpdateUserDTO;
import com.ecotech.api.controller.dto.UserResponseDTO;
import com.ecotech.api.controller.mappers.UserMapper;
import com.ecotech.api.model.User;
import com.ecotech.api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import static com.ecotech.api.config.OpenApiConfiguration.BEARER_AUTH;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users")
@SecurityRequirement(name = BEARER_AUTH)
public class UserController implements GenericController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    @Operation(summary = "Cria um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario criado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "409", description = "Usuario ja cadastrado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> create(@RequestBody @Valid CreateUserDTO createUserDTO) {
        User user = userMapper.toEntity(createUserDTO);
        User savedUser = userService.save(user);
        URI location = generateLocationHeader(savedUser.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #p0.toString() == authentication.name")
    @Operation(summary = "Busca um usuario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        User user = userService.findById(id);

        UserResponseDTO response = userMapper.toResponseDTO(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios listados"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<List<UserResponseDTO>> findAll() {

        List<UserResponseDTO> users = userService
                .findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #p0.toString() == authentication.name")
    @Operation(summary = "Atualiza um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "409", description = "Usuario ja cadastrado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody @Valid UpdateUserDTO updateUserDTO) {
        User user = userService.findById(id);
        userMapper.updateEntity(updateUserDTO, user);
        userService.update(user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #p0.toString() == authentication.name")
    @Operation(summary = "Remove um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Identificador invalido"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        User user = userService.findById(id);
        userService.delete(user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca o usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado encontrado"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<UserResponseDTO> findAuthenticatedUser(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        UserResponseDTO response = userMapper.toResponseDTO(
                userService.findById(userId));

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Altera a senha do usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordDTO changePasswordDTO,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        userService.changePassword(authenticatedUserId, changePasswordDTO);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza a imagem de perfil do usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem de perfil atualizada"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "422", description = "Arquivo invalido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> updateProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        userService.updateProfileImage(
                authenticatedUserId, file);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/cover-image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza a imagem de capa do usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem de capa atualizada"),
            @ApiResponse(responseCode = "401", description = "Autenticacao necessaria ou token invalido"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado"),
            @ApiResponse(responseCode = "422", description = "Arquivo invalido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> updateCoverImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        UUID authenticatedUserId = UUID.fromString(authentication.getName());

        userService.updateCoverImage(
                authenticatedUserId,
                file);

        return ResponseEntity.noContent().build();
    }
}
