package com.ecotech.api.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ecotech.api.model.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_users_username",
            columnNames = "username"
        ),
        @UniqueConstraint(
            name = "uk_users_email",
            columnNames = "email"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column( nullable = false, length = 30)
    private String username;

    @Column( nullable = false, length = 150)
    private String email;

    @Column( nullable = false, length = 255)
    private String password;

    @Column( nullable = false, length = 100)
    private String name;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;


    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column( length = 100)
    private String city;

    @Column( length = 255)
    private String website;

    @Column( length = 500)
    private String bio;

    @Column( nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

     @PrePersist
    private void prePersist() {
        if (active == null) {
            active = true;
        }

        if (role == null) {
            role = UserRole.USER;
        }
    }
}
