package domain.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private Boolean active;
}