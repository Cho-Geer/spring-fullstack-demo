package main.java.mysql_jpa;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Role entity representing a user role in the system.
 * 
 * @author zhaogang
 * @version 1.0
 * @since 2023-12-01
 */
@Data
@Entity
@Table(name = "roles")
@Schema(description = "Role entity representing a user role in the system")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false, unique = true)
    @Schema(description = "Unique identifier for the role", example = "1")
    private Long roleId;

    @Column(name = "role_name", length = 20, nullable = false, unique = true)
    @Schema(description = "Name of the role", example = "ADMIN", maxLength = 20)
    private String roleName;

    @Column(name = "description", length = 200)
    @Schema(description = "Description of the role", example = "Administrator role with full access", maxLength = 200)
    private String description;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Schema(description = "Timestamp when the role was created", example = "2023-12-01T10:30:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Schema(description = "Timestamp when the role was last updated", example = "2023-12-01T15:45:00")
    private LocalDateTime updatedAt;
}
