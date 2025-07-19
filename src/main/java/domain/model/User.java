package domain.model;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    private UUID uuid;

    private String username;

    private String email;

    private String passwordHash;

    private Boolean active;

    private Role role;
}