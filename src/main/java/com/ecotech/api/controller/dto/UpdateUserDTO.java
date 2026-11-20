package com.ecotech.api.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateUserDTO(

        @NotBlank(message = "O nome de usuário é obrigatório.")
        @Size(min = 3, max = 30, message = "O nome de usuário deve possuir entre 3 e 30 caracteres.")
        String username,

        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 100, message = "O nome deve possuir entre 2 e 100 caracteres.")
        String name,

        @Size(max = 500, message = "A URL da imagem de capa deve possuir no máximo 500 caracteres.")
        @Schema(accessMode = Schema.AccessMode.READ_ONLY,
                description = "Ignorado nesta operacao. Altere a capa por PATCH /users/me/cover-image.")
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String coverImageUrl,

        @Size(max = 500, message = "A URL da imagem de perfil deve possuir no máximo 500 caracteres.") 
        @Schema(accessMode = Schema.AccessMode.READ_ONLY,
                description = "Ignorado nesta operacao. Altere a foto por PATCH /users/me/profile-image.")
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String profileImageUrl,

        @Size(max = 100, message = "A cidade deve possuir no máximo 100 caracteres.") 
        String city,

        @Size(max = 255, message = "O site deve possuir no máximo 255 caracteres.") 
        String website,

        @Size(max = 500, message = "A biografia deve possuir no máximo 500 caracteres.") 
        String bio
    ) {

}
