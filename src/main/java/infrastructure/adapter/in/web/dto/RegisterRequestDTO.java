package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email debe tener menos de 255 caracteres")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 3, max = 100, message = "La contraseña debe tener entre 3 y 100 caracteres")
    private String password;

    @NotEmpty(message = "Roles set cannot be empty.")
    private Set<String> roles;
}