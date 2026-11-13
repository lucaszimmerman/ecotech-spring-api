package com.ecotech.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI ecoTechOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EcoTech API")
                        .version("v1")
                        .description("""
                                API REST da rede social EcoTech, focada em sustentabilidade.

                                Cadastre-se em POST /auth/register ou entre em POST /auth/login.
                                Copie o accessToken e informe apenas o token no botao Authorize (bearerAuth).
                                As operacoes de usuarios e posts exigem autenticacao; operacoes administrativas exigem ADMIN.

                                Listagens usam page (inicia em 0), size (maximo 100) e sort (campo,direcao).
                                Posts incluem likeCount, commentCount e likedByCurrentUser.
                                A verificacao de email e solicitada apos o cadastro, por acao do usuario.
                                """))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
