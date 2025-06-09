package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

        @NotBlank
        private String role;

        @NotBlank
        private boolean active;
    }
