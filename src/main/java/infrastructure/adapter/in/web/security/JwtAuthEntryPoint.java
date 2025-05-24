package infrastructure.adapter.in.web.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Esto se invocará cuando un usuario no autenticado intente acceder a un recurso protegido
        // y se le niegue el acceso.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Acceso denegado: Necesitas autenticación.");
    }
}
