package com.example.demo_mysql;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
        name = "role_id",
        nullable = false,
        unique = true
    )
    private Long roleId;

    @Column(
        name = "role_name",
        length = 20,
        nullable = false,
        unique = true
    )
    private String roleName;

    @Column(
        name = "description",
        length = 200,
        nullable = true,
        unique = false
    )
    private String description;

    @Column(
        name = "created_at",
        nullable = false,
        unique = false,
        updatable = false
    )
    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false,
        unique = false
    )
    @org.hibernate.annotations.UpdateTimestamp
    private LocalDateTime updatedAt;

}
