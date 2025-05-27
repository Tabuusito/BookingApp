package domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "usuarios") //@Entity y @Table acoplan el dominio con JPA,
                        // añadir luego a adapter.out.persistence.entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    //Spring mapea por defecto campos camelCase a snake_case por convención
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column()
    private Boolean active = Boolean.TRUE;
}