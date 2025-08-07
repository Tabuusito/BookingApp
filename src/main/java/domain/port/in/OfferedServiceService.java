package domain.port.in;

import domain.exception.*;
import domain.model.OfferedService;

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
     * @param ownerUuid        El UUID del usuario que será el propietario del nuevo servicio.
     * @return El servicio ofrecido creado y persistido.
     * @throws DuplicateServiceNameException Si ya existe un servicio con el mismo nombre.
     * @throws UserNotFoundException Si el 'ownerId' no corresponde a un usuario existente.
     * @throws org.springframework.security.access.AccessDeniedException Si el 'requester' no tiene permiso para crear un servicio para el 'ownerId' especificado.
     */
    OfferedService createOfferedService(OfferedService offeredService, UUID ownerUuid);

    /**
     * Busca un servicio ofrecido por su ID, aplicando reglas de autorización.
     *
     * @param serviceUuid El ID del servicio a buscar.
     *                  si el solicitante es un administrador o el propietario del servicio.
     * @return Un {@link Optional} con el servicio si se encuentra y está autorizado, o {@link Optional#empty()} si no.
     * @throws org.springframework.security.access.AccessDeniedException Si el 'requester' no tiene permiso para ver este servicio.
     */
    Optional<OfferedService> findOfferedServiceByUuid(UUID serviceUuid);

    /**
     * Actualiza un servicio ofrecido existente, aplicando reglas de autorización.
     *
     * @param serviceUuid  El ID del servicio a actualizar.
     * @param updateData Un objeto de dominio OfferedService con los campos a actualizar (comportamiento PATCH).
     * @return Un {@link Optional} con el servicio actualizado si se encuentra y la operación es exitosa y autorizada.
     * @throws OfferedServiceNotFoundException Si el servicio con el ID especificado no existe.
     * @throws DuplicateServiceNameException Si se intenta cambiar el nombre a uno que ya existe.
     * @throws org.springframework.security.access.AccessDeniedException Si el 'requester' no tiene permiso para modificar este servicio.
     */
    Optional<OfferedService> updateOfferedService(UUID serviceUuid, OfferedService updateData);

    /**
     * Elimina un servicio ofrecido, aplicando reglas de autorización.
     *
     * @param serviceUuid El UUID del servicio a eliminar.
     * @throws ServiceInUseException Si el servicio tiene reservas asociadas y no se puede eliminar.
     * @throws org.springframework.security.access.AccessDeniedException Si el 'requester' no tiene permiso para eliminar este servicio.
     */
    void deleteOfferedService(UUID serviceUuid);

    /**
     * Lista todos los servicios ofrecidos en el sistema (generalmente para administradores).
     *
     *                  verificar si el solicitante tiene permisos de administrador.
     * @return Una lista de todos los servicios.
     * @throws org.springframework.security.access.AccessDeniedException Si el solicitante no es un administrador.
     */
    List<OfferedService> findAllServices();

    public List<OfferedService> findAllActiveServices();

    /**
     * Lista los servicios ofrecidos que pertenecen al usuario autenticado.
     *
     * @param nameContains Opcional. Fragmento de nombre para filtrar.
     * @param activeOnly   `true` para obtener solo los servicios activos del usuario, `false` para todos.
     * @return Una lista de servicios que pertenecen al usuario autenticado y que coinciden con los filtros.
     * @throws org.springframework.security.access.AccessDeniedException Si el solicitante no está autenticado.
     */
    List<OfferedService> findMyServices(String nameContains, boolean activeOnly);

    public List<OfferedService> findAllServicesForAdmin(String nameContains, Optional<UUID> ownerUuid, boolean activeOnly);

    /**
     * Busca todos los servicios activos de un proveedor específico.
     * @param providerUuid El UUID del proveedor.
     * @return Una lista de servicios activos.
     */
    List<OfferedService> findAllActiveServicesByProvider(UUID providerUuid);
}
