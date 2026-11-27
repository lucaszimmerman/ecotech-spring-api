<div align="center">

# EcoTech API

**API REST para uma rede social voltada à sustentabilidade.**

Perfis, publicações e conexões, com autenticação JWT, login com Google e imagens em armazenamento privado.

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

[**Acessar a EcoTech ↗**](https://ecotech-angular.vercel.app) · [Explorar no Swagger](https://ecotech-spring-api.onrender.com/swagger-ui/index.html) · [OpenAPI](https://ecotech-spring-api.onrender.com/v3/api-docs) · [CI](https://github.com/lucaszimmerman/ecotech-spring-api/actions/workflows/ci.yml)

</div>

[Sobre](#sobre-o-projeto) · [Demonstração](#demonstração) · [Funcionalidades](#funcionalidades) · [Tecnologias](#tecnologias) · [Arquitetura](#arquitetura-e-decisões-técnicas) · [Qualidade](#qualidade-e-integração-contínua) · [Evolução](#evolução-planejada)

> [!IMPORTANT]
> **Aguarde a inicialização no primeiro acesso.** A demonstração utiliza o Render Free, que suspende o serviço após 15 minutos sem tráfego. A próxima requisição inicia a aplicação novamente e pode levar cerca de um minuto, com variações. Se o Swagger exibir uma tela de carregamento ou a chamada atingir o tempo limite, aguarde e tente novamente. [Sobre a inicialização no Render](https://render.com/docs/free#spinning-down-on-idle).

## Sobre o projeto

EcoTech é um projeto de portfólio que implementa o backend de uma rede social para compartilhar conteúdo sobre sustentabilidade. Os usuários podem criar publicações, descobrir pessoas, acompanhar perfis e interagir por curtidas e comentários.

O projeto aplica regras de autorização por recurso, persistência relacional com migrations e integração com serviços de autenticação e armazenamento. A aplicação está publicada em [ecotech-angular.vercel.app](https://ecotech-angular.vercel.app), com o frontend em Angular no repositório [ecotech-angular](https://github.com/lucaszimmerman/ecotech-angular). A API também pode ser explorada diretamente pelo Swagger.

## Demonstração

1. Abra o [Swagger da EcoTech](https://ecotech-spring-api.onrender.com/swagger-ui/index.html) e aguarde o serviço iniciar.
2. Crie sua conta em **`POST /auth/register`**.
3. Copie o campo **`accessToken`** retornado.
4. Clique em **Authorize**, cole apenas o token e confirme.
5. Consulte seu perfil em **`GET /users/me`**, crie uma publicação e explore as operações sociais.

Para entrar novamente, use **`POST /auth/login`** com seu `username` e sua senha.

O login com Google está disponível na [interface web](https://ecotech-angular.vercel.app/login). Ao concluir o fluxo, a API redireciona para o frontend com um código de uso único, válido por 60 segundos, que é trocado pelo token em **`POST /auth/google/exchange`**. O JWT não aparece na URL.

Para explorar o feed, cadastre duas contas, publique com uma delas e siga esse perfil com a outra. O endpoint **`GET /posts/feed`** retorna as publicações das pessoas seguidas.

> [!NOTE]
> O Swagger apresenta as operações disponíveis para teste com uma conta comum. Fluxos de e-mail e a listagem exclusiva de administrador estão ocultos. O cadastro permite utilizar os recursos sociais sem verificar o e-mail imediatamente.

## Funcionalidades

| Recurso | Comportamento |
| --- | --- |
| **Autenticação** | Cadastro, login com JWT e autenticação com Google via OAuth2/OpenID Connect |
| **Perfis** | Consulta e edição do próprio perfil, foto, capa e consulta de perfis públicos |
| **Descoberta** | Busca paginada de pessoas por nome ou username |
| **Conexões** | Seguir, deixar de seguir e consultar seguidores e pessoas seguidas |
| **Publicações** | Criar, consultar, editar e excluir posts, com suporte a imagens |
| **Feed** | Acompanhar publicações de pessoas seguidas |
| **Interações** | Curtir, remover curtida e gerenciar comentários |
| **Resumo dos posts** | Contagens de curtidas e comentários e estado de curtida do usuário autenticado |

Os perfis públicos omitem o e-mail e exigem autenticação para consulta. Edições e exclusões respeitam as permissões de proprietário ou administrador.

### Imagens com acesso temporário

Fotos, capas e imagens de posts ficam em um bucket privado no Amazon S3. A API retorna URLs pré-assinadas com validade padrão de 15 minutos.

Se uma imagem deixar de abrir após esse período, consulte o recurso novamente para receber uma nova URL. A URL completa pode ser usada para visualizar a imagem, sem enviar o JWT ao S3.

## Tecnologias

| Categoria | Tecnologias |
| --- | --- |
| **Backend** | Java 21 · Spring Boot 4.1 · Spring Web MVC |
| **Segurança** | Spring Security · JWT/HS256 · BCrypt · OAuth2 · OpenID Connect |
| **Dados** | PostgreSQL · Spring Data JPA · Hibernate · Flyway |
| **Contratos** | Bean Validation · MapStruct · OpenAPI/Swagger |
| **Testes** | JUnit · Mockito · MockMvc · AssertJ · H2 |
| **Build e automação** | Maven · GitHub Actions |
| **Infraestrutura** | Docker · Docker Compose |
| **Hospedagem e integrações** | Render · Neon PostgreSQL · Amazon S3 · Google OAuth2 |

## Arquitetura e decisões técnicas

O backend é organizado como um monólito em camadas, com responsabilidades distribuídas entre a interface HTTP, os serviços da aplicação e a persistência.

| Camada | Responsabilidade |
| --- | --- |
| `controller` | Endpoints REST e tratamento centralizado de erros |
| `controller/dto` e `controller/mappers` | Contratos da API e conversões com MapStruct |
| `service` e `validator` | Casos de uso, transações e regras de negócio |
| `repository` e `model` | Consultas, entidades e relacionamentos |
| `security` e `config` | Autenticação, autorização e configuração das integrações |

Decisões presentes na implementação:

- **DTOs distintos para dados públicos e privados**, evitando expor informações pessoais nas consultas de outros perfis.
- **Autorização por role e propriedade do recurso**, além da validação do JWT.
- **Migrations com Flyway**, mantendo a evolução do schema versionada.
- **Consultas de interações em lote**, evitando uma consulta adicional por post para compor os resumos das listagens.
- **Paginação nas consultas sociais**, com limite de até 100 itens por página.
- **URLs temporárias para imagens**, mantendo o bucket privado.

## Qualidade e integração contínua

A suíte inclui testes de regras de negócio, validações, autorização, endpoints, consultas de persistência e contratos OpenAPI, além de um fluxo social com JWT.

Os testes utilizam H2 e simulações das integrações externas. O GitHub Actions compila a aplicação e executa a suíte nos pushes e pull requests configurados no workflow. Essa validação não substitui os testes das integrações reais com os provedores.

[Consultar as execuções do CI ↗](https://github.com/lucaszimmerman/ecotech-spring-api/actions/workflows/ci.yml)

## Disponibilidade e escopo

**Hospedagem gratuita.** A demonstração está sujeita aos tempos de inicialização, reinicializações e limites do Render Free. Os dados ficam no Neon e as imagens no S3, separados do armazenamento local do serviço.

**Envio de e-mails.** A integração com Amazon SES está implementada para verificação de e-mail e recuperação de senha. A liberação para envio a destinatários não verificados está pendente; esses fluxos estão ocultos no Swagger e não fazem parte da demonstração pública.

**Interface.** Este repositório contém a API. A interface web fica em [ecotech-angular](https://github.com/lucaszimmerman/ecotech-angular) e está publicada na Vercel; os recursos também podem ser explorados pelo Swagger ou por um cliente HTTP, como o Postman.

## Evolução planejada

- Liberar os envios de e-mail e tratar devoluções e denúncias de spam.
- Adicionar limitação de requisições nos endpoints de autenticação e envio de e-mails.
- Complementar os testes de persistência com PostgreSQL via Testcontainers.
- Evoluir a renovação e a revogação de sessões.

---

<div align="center">

Desenvolvido por [Lucas Zimmerman](https://github.com/lucaszimmerman)

</div>

