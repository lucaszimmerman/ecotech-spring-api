package com.ecotech.api.repository;

import java.util.Optional;
import java.util.UUID;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecotech.api.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Sobrescreve o findById padrão herdado de JpaRepository.
    //
    // O objetivo aqui não é mudar o comportamento de busca por ID,
    // mas adicionar uma estratégia específica de carregamento para essa consulta.
    //
    // Sem esse override, JpaRepository continuaria fornecendo findById normalmente,
    // porém sem o @EntityGraph definido abaixo.
    @Override
    // Carrega o relacionamento "user" junto com o Comment nesta consulta.
    //
    // Como Comment.user está configurado com FetchType.LAZY,
    // o Hibernate normalmente não carrega os dados completos do usuário
    // ao buscar um comentário.
    //
    // Isso pode causar LazyInitializationException caso, depois que a
    // transação/session já tenha sido encerrada, o Mapper tente acessar:
    //
    // comment.getUser().getUsername()
    // comment.getUser().getName()
    // etc.
    //
    // Com @EntityGraph(attributePaths = "user"), informamos ao JPA/Hibernate:
    // "Para esta consulta específica, carregue também o usuário associado."
    //
    // Assim mantemos o relacionamento LAZY como padrão, mas carregamos
    // o User apenas quando realmente precisamos dele.
    @EntityGraph(attributePaths = "user")
    Optional<Comment> findById(UUID id);
    
     // Lista os comentários de um determinado post com paginação.
    //
    // Também carregamos o "user" junto com cada Comment porque a resposta
    // da API provavelmente exibirá informações do autor do comentário,
    // como username, nome e foto de perfil.
    //
    // Isso evita acessar um proxy LAZY de User depois que a sessão
    // do Hibernate já foi encerrada.
    @EntityGraph(attributePaths = "user")
    Page<Comment> findByPostId(UUID postId, Pageable pageable);

    Page<Comment> findByUserId(UUID userId, Pageable pageable);

    boolean existsByIdAndUserId(UUID commentId, UUID userId);

    boolean existsByIdAndPostIdAndUserId(
            UUID commentId,
            UUID postId,
            UUID userId);

    long countByPostId(UUID postId);

    @EntityGraph(attributePaths = "user")
    Optional<Comment> findByIdAndPostId(
            UUID id,
            UUID postId);
}
