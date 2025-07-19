package infrastructure.adapter.out.persistence.entity;

import domain.model.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_uuid", unique = true, nullable = false, length = 36, updatable = false)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false)
    private Role role;

    @Column
    private Boolean active;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }
}