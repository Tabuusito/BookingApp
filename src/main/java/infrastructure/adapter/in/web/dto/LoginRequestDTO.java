package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    private String username;

    @Size(min = 3, max = 100, message = "La contraseña debe tener entre 3 y 100 caracteres")
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}
