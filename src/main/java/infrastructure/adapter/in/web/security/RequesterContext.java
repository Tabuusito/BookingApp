package infrastructure.adapter.in.web.security;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Representa el contexto de seguridad del solicitante de una operación,
 * desacoplado de cualquier framework específico.
 *
 * @param userId El ID del usuario autenticado, si existe.
 * @param roles  El conjunto de roles asignados al usuario (ej. "ROLE_ADMIN", "ROLE_USER").
 */
public record RequesterContext(Optional<Long> userId, Set<String> roles) {

    /**
     * Constructor compacto para asegurar que los roles nunca sean nulos.
     */
    public RequesterContext {
        if (roles == null) {
            roles = Collections.emptySet();
        }
    }

    /**
     * Verifica si el solicitante tiene el rol de administrador.
     * @return `true` si el conjunto de roles contiene "ROLE_ADMIN", `false` en caso contrario.
     */
    public boolean isAdmin() {
        return roles.contains("ROLE_ADMIN");
    }

    /**
     * Comprueba si el ID del solicitante coincide con un ID de usuario determinado.
     *
     * @param otherUserId El ID de usuario con el que comparar.
     * @return `true` si el solicitante está autenticado y su ID coincide con otherUserId, `false` en caso contrario.
     */
    public boolean isOwner(Long otherUserId) {
        return userId.map(id -> id.equals(otherUserId)).orElse(false);
    }
}
