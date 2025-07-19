package domain.port.in;

import domain.exception.*;
import domain.model.OfferedService;
import infrastructure.adapter.in.web.security.RequesterContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para gestionar la lógica de negocio de los servicios ofrecidos.
 * Define los casos de uso disponibles para los servicios en la aplicación.
 */
public interface OfferedServiceService {

    /**
     * Crea un nuevo servicio ofrecido, asignándolo a un usuario propietario.
     * La lógica de autorización determinará si el 'requester' puede crear un servicio para el 'ownerId' especificado.
     *
     * @param offeredService El objeto de dominio OfferedService con los detalles del servicio a crear.
     *                       No debe incluir el ID del propietario; este será asignado por el servicio.
     * @param ownerId        El ID del usuario que será el propietario del nuevo servicio.
     * @param requester      El contexto de seguridad del usuario que realiza la petición.
     * @return El servicio ofrecido creado y persistido.
     * @throws DuplicateServiceNameException Si ya existe un servicio con el mismo nombre.
     * @throws UserNotFoundException Si el 'ownerId' no corresponde a un usuario existente.
     * @throws org.springframework.security.access.AccessDeniedException Si el 'requester' no tiene permiso para crear un servicio para el 'ownerId' especificado.
     */
    OfferedService createOfferedService(OfferedService offeredService, Long ownerId, RequesterContext requester);

    /**
     * Busca un servicio ofrecido por su ID, aplicando reglas de autorización.
     *
     * @param serviceUuid El ID del servicio a buscar.
     * @param requester El contexto de seguridad del usuario que solicita la información. El servicio verificará
     *                  si el solicitante es un administrador o el propietario del servicio.
     * @return Un {@link Optional} con el servicio si se encuentra y está autorizado, o {@link Optional#empty()} si no.
     * @throws org.springframework.security.access.AccessDeniedException Si el 'requester' no tiene permiso para ver este servicio.
     */
    Optional<OfferedService> findOfferedServiceByUuid(UUID serviceUuid, RequesterContext requester);

    /**
     * Actualiza un servicio ofrecido existente, aplicando reglas de autorización.
     *
     * @param serviceUuid  El ID del servicio a actualizar.
     * @param updateData Un objeto de dominio OfferedService con los campos a actualizar (comportamiento PATCH).
     * @param requester  El contexto de seguridad del usuario que realiza la petición.
     * @return Un {@link Optional} con el servicio actualizado si se encuentra y la operación es exitosa y autorizada.
     * @throws OfferedServiceNotFoundException Si el servicio con el ID especificado no existe.
     * @throws DuplicateServiceNameException Si se intenta cambiar el nombre a uno que ya existe.
     * @throws org.springframework.security.access.AccessDeniedException Si el 'requester' no tiene permiso para modificar este servicio.
     */
    Optional<OfferedService> updateOfferedService(UUID serviceUuid, OfferedService updateData, RequesterContext requester);

    /**
     * Elimina un servicio ofrecido, aplicando reglas de autorización.
     *
     * @param serviceUuid El ID del servicio a eliminar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return `true` si se eliminó, `false` si no se encontró.
     * @throws ServiceInUseException Si el servicio tiene reservas asociadas y no se puede eliminar.
     * @throws org.springframework.security.access.AccessDeniedException Si el 'requester' no tiene permiso para eliminar este servicio.
     */
    boolean deleteOfferedService(UUID serviceUuid, RequesterContext requester);

    /**
     * Lista todos los servicios ofrecidos en el sistema (generalmente para administradores).
     *
     * @param requester El contexto de seguridad del usuario que realiza la petición. Se utilizará para
     *                  verificar si el solicitante tiene permisos de administrador.
     * @return Una lista de todos los servicios.
     * @throws org.springframework.security.access.AccessDeniedException Si el solicitante no es un administrador.
     */
    List<OfferedService> findAllServices(RequesterContext requester);

    /**
     * Lista todos los servicios ofrecidos que están marcados como activos.
     *
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return Una lista de servicios activos.
     * @throws org.springframework.security.access.AccessDeniedException Si el solicitante no tiene permiso para ver todos los servicios activos.
     */
    List<OfferedService> findAllActiveServices(RequesterContext requester);

    /**
     * Busca servicios por un nombre que contenga el texto proporcionado, filtrando por estado de activación y aplicando autorización.
     *
     * @param nameFragment Fragmento del nombre a buscar.
     * @param activeOnly   `true` para buscar solo entre servicios activos, `false` para buscar entre todos.
     * @param requester    El contexto de seguridad del usuario que realiza la petición.
     * @return Una lista de servicios que coinciden con el criterio de búsqueda.
     * @throws org.springframework.security.access.AccessDeniedException Si el solicitante no tiene permiso para realizar esta búsqueda.
     */
    List<OfferedService> findServicesByNameContaining(String nameFragment, boolean activeOnly, RequesterContext requester);

    /**
     * Lista los servicios ofrecidos que pertenecen al usuario autenticado.
     *
     * @param nameContains Opcional. Fragmento de nombre para filtrar.
     * @param activeOnly   `true` para obtener solo los servicios activos del usuario, `false` para todos.
     * @param requester    El contexto de seguridad del usuario que realiza la petición. Se espera que el usuario esté autenticado.
     * @return Una lista de servicios que pertenecen al usuario autenticado y que coinciden con los filtros.
     * @throws org.springframework.security.access.AccessDeniedException Si el solicitante no está autenticado.
     */
    List<OfferedService> findMyServices(String nameContains, boolean activeOnly, RequesterContext requester);

    public List<OfferedService> findAllServicesForAdmin(String nameContains, boolean activeOnly, Optional<Long> ownerId, RequesterContext requester);
}
