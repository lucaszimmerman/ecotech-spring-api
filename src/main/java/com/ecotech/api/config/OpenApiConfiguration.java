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
                                As operacoes de usuarios e posts exigem autenticacao.
                                Esta documentacao apresenta os recursos disponiveis para teste com uma conta comum.
                                Operacoes exclusivas de administrador e fluxos de email indisponiveis na demonstracao foram omitidos.

                                Listagens usam page (inicia em 0), size (maximo 100) e sort (campo,direcao).
                                Posts incluem likeCount, commentCount e likedByCurrentUser.
                                Nao e necessario verificar o email para testar os recursos apresentados.

                                Imagens do S3 sao retornadas como URLs GET pre-assinadas, com validade padrao de 15 minutos.
                                Use a URL completa diretamente no src da imagem, sem enviar o JWT para o S3.
                                Se a URL expirar, consulte novamente o endpoint da API para obter uma nova.
                                Ausencia de imagem e representada por null. Nao persista URLs temporarias no frontend.
                                Apos upload de foto ou capa (204), consulte GET /users/me para obter as URLs atualizadas.
                                Campos de imagem sao ignorados no PUT /users/{id}; use os endpoints multipart dedicados.
                                """))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
