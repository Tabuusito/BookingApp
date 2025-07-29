package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class AdminUserCreationDTO {

        @NotBlank
        @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
        private String username;

        @NotBlank
        @Size(min = 3, max = 100, message = "La contraseña debe tener entre 3 y 100 caracteres")
        private String password;

        @NotBlank
        @Email(message = "El email debe ser válido")
        @Size(max = 255, message = "El email debe tener menos de 255 caracteres")
        private String email;

        @NotEmpty
        private Set<String> roles;

        @NotNull(message = "El estado activo no puede ser nulo")
        private boolean active;
    }
