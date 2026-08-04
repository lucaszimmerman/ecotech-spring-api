package com.ecotech.api.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_follows",
// Garante que a mesma combinação de usuário seguidor + usuário seguido 
// não possa ser cadastrada mais de uma vez no banco.
// 
// Exemplo:
// Lucas -> Maria pode existir uma vez.
// Lucas -> Maria novamente será rejeitado pelo banco.
    uniqueConstraints = {
        @UniqueConstraint(
        name = "uk_user_follows_follower_followed",
        columnNames = {"follower_id", "followed_id",}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // Muitos registros de UserFollow podem possuir o mesmo usuário como seguidor.
    //
    // Exemplo:
    // Lucas -> Maria
    // Lucas -> João
    // Lucas -> Pedro
    // Nesse caso, vários registros apontam para o mesmo User (Lucas)
    // através do campo follower.
    // FetchType.LAZY evita carregar os dados completos do usuário
    // automaticamente sempre que um UserFollow for buscado.
    // optional = false indica, no nível da entidade JPA,
    // que um UserFollow obrigatoriamente precisa possuir um follower.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // Define a coluna responsável pela chave estrangeira.
    // 
    // follower_id guarda o ID do usuário que está seguindo alguém.
    //
    // nullable = false impede que essa coluna seja NULL no banco.
    // 
    // foreignKey apenas define explicitamente o nome da constraint 
    // de chave estrangeira criada no banco.
    @JoinColumn(
        name = "follower_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_user_follows_follower")
    )
    private User follower;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "followed_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_user_follows_followed")
    )
    private User followed;


    // Preenchido automaticamente pelo Spring Data JPA
    // no momento em que o relacionamento é criado.
    //
    // Exemplo: 
    // Lucas começou a seguir Maria em 31/07/2026 às 14:30.
    // 
    // updatable = false impede que essa data seja alterada
    // posteriormente em operações de UPDATE.
    @CreatedDate
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;
}
