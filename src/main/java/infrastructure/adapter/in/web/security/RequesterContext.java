package infrastructure.adapter.in.web.security;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Representa el contexto de seguridad del solicitante de una operación,
 * desacoplado de cualquier framework específico.
 *
 * @param userUuid El UUID del usuario autenticado, si existe.
 * @param roles  El conjunto de roles asignados al usuario (ej. "ROLE_ADMIN", "ROLE_USER").
 */
public record RequesterContext(Optional<UUID> userUuid, Set<String> roles) {

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
     * Comprueba si el UUID del solicitante coincide con un ID de usuario determinado.
     *
     * @param otherUserUuid El ID de usuario con el que comparar.
     * @return `true` si el solicitante está autenticado y su UUID coincide con otherUserUuid, `false` en caso contrario.
     */
    public boolean isOwner(UUID otherUserUuid) {
        return userUuid.map(uuid -> uuid.equals(otherUserUuid)).orElse(false);
    }
}
