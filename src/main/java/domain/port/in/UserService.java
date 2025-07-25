package domain.port.in;

import domain.model.User;
import domain.exception.*;
import org.springframework.security.access.AccessDeniedException;
import infrastructure.adapter.in.web.dto.RegisterRequestDTO;
import infrastructure.adapter.in.web.security.RequesterContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface UserService {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username El nombre de usuario a buscar.
     * @return El usuario encontrado.
     * @throws UserNotFoundException Si el usuario no es encontrado.
     */
    User findByUsername(String username);

    /**
     * Crea un nuevo usuario en el sistema.
     * Solo los administradores pueden crear usuarios arbitrarios.
     *
     * @param newUser Datos del nuevo usuario a crear.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return El usuario creado.
     * @throws DuplicateUserInfoException Si el nombre de usuario o el email ya están en uso.
     * @throws AccessDeniedException Si el 'requester' no tiene permiso para crear usuarios.
     * @throws IllegalArgumentException Si los datos proporcionados no son válidos (ej. campos en blanco).
     */
    User createUser(User newUser, RequesterContext requester);

    /**
     * Actualiza un usuario existente.
     * Un administrador puede actualizar cualquier usuario. Un usuario normal solo puede actualizar su propio perfil.
     *
     * @param userToUpdate Datos del usuario con los campos a actualizar, incluyendo el ID del usuario a modificar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return El usuario actualizado.
     * @throws UserNotFoundException Si el usuario a actualizar no es encontrado.
     * @throws AccessDeniedException Si el 'requester' no tiene permiso para actualizar este usuario o su rol/estado.
     * @throws DuplicateUserInfoException Si el nuevo nombre de usuario o email ya están en uso.
     * @throws IllegalArgumentException Si no se provee un ID para la actualización.
     */
    User updateUser(User userToUpdate, RequesterContext requester);

    /**
     * Busca un usuario por su ID, aplicando reglas de autorización.
     * Un administrador puede acceder a cualquier perfil de usuario. Un usuario normal solo puede acceder a su propio perfil.
     *
     * @param id El ID del usuario a buscar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return El usuario encontrado.
     * @throws UserNotFoundException Si el usuario no es encontrado.
     * @throws AccessDeniedException Si el 'requester' no tiene permiso para acceder a este usuario.
     */
    User findUserById(Long id, RequesterContext requester);

    User findUserByUuid(UUID uuid, RequesterContext requester);

    /**
     * Elimina un usuario por su ID, aplicando reglas de autorización.
     * Un administrador puede eliminar cualquier usuario. Un usuario normal solo puede eliminar su propio perfil.
     *
     * @param id El ID del usuario a eliminar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @throws UserNotFoundException Si el usuario a eliminar no es encontrado.
     * @throws AccessDeniedException Si el 'requester' no tiene permiso para eliminar este usuario.
     */
    void deleteUserById(Long id, RequesterContext requester);

    void deleteUserByUuid(UUID uuid, RequesterContext requester);

    /**
     * Lista todos los usuarios del sistema.
     * Esta operación está restringida a usuarios con rol de administrador.
     *
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return Una lista de todos los usuarios.
     * @throws AccessDeniedException Si el 'requester' no es un administrador.
     */
    List<User> getAllUsers(RequesterContext requester);

    // Métodos para la gestión del propio usuario (ej. /api/me/user)
    /**
     * Busca el perfil del usuario autenticado.
     * @param requester El contexto de seguridad del usuario.
     * @return El perfil del usuario autenticado.
     * @throws AccessDeniedException Si el usuario no está autenticado (no hay ID en el RequesterContext).
     * @throws UserNotFoundException Si el usuario autenticado no es encontrado en la base de datos (inconsistencia de datos).
     */
    User getMyProfile(RequesterContext requester);

    /**
     * Actualiza el perfil del usuario autenticado.
     * @param userToUpdate Los datos del perfil a actualizar.
     * @param requester El contexto de seguridad del usuario.
     * @return El perfil actualizado.
     * @throws AccessDeniedException Si el usuario no está autenticado.
     * @throws UserNotFoundException Si el usuario autenticado no es encontrado.
     * @throws DuplicateUserInfoException Si el nombre de usuario o email ya están en uso.
     */
    User updateMyProfile(User userToUpdate, RequesterContext requester);

    /**
     * Elimina el perfil del usuario autenticado.
     * @param requester El contexto de seguridad del usuario.
     * @throws AccessDeniedException Si el usuario no está autenticado.
     * @throws UserNotFoundException Si el usuario autenticado no es encontrado.
     */
    void deleteMyProfile(RequesterContext requester);

}
