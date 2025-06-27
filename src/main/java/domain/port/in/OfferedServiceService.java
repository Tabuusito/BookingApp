package domain.port.in;


import domain.model.OfferedService;
import domain.exception.*;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada para gestionar la lógica de negocio de los servicios ofrecidos.
 * Define los casos de uso disponibles para los servicios en la aplicación.
 */
public interface OfferedServiceService {

    /**
     * Crea un nuevo servicio ofrecido.
     *
     * @param offeredService El objeto de dominio OfferedService a crear, mapeado desde el DTO.
     * @return El servicio ofrecido creado y persistido.
     */
    OfferedService createOfferedService(OfferedService offeredService);

    /**
     * Busca un servicio ofrecido por su ID.
     *
     * @param serviceId El ID del servicio.
     * @return Un {@link Optional} con el servicio si se encuentra, o {@link Optional#empty()} si no.
     */
    Optional<OfferedService> findOfferedServiceById(Long serviceId);

    /**
     * Actualiza un servicio ofrecido existente.
     *
     * @param serviceId      El ID del servicio a actualizar.
     * @param updateData     Un objeto de dominio OfferedService con los campos actualizados.
     * @return Un {@link Optional} con el servicio actualizado, o {@link Optional#empty()} si el servicio no se encuentra.
     * @throws OfferedServiceNotFoundException Si el servicio con el ID especificado no existe.
     */
    Optional<OfferedService> updateOfferedService(Long serviceId, OfferedService updateData);

    /**
     * Elimina un servicio ofrecido.
     *
     * @param serviceId El ID del servicio a eliminar.
     * @return `true` si se eliminó, `false` si no se encontró.
     * @throws ServiceInUseException Si el servicio tiene reservas asociadas y no se puede eliminar.
     */
    boolean deleteOfferedService(Long serviceId);

    /**
     * Lista todos los servicios ofrecidos en el sistema.
     * Esta operación puede estar restringida a ciertos roles (ej. ADMIN).
     *
     * @return Una lista de todos los servicios.
     */
    List<OfferedService> findAllServices();

    /**
     * Lista todos los servicios ofrecidos que están marcados como activos.
     *
     * @return Una lista de servicios activos.
     */
    List<OfferedService> findAllActiveServices();

    /**
     * Busca servicios por un nombre que contenga el texto proporcionado.
     *
     * @param nameFragment Fragmento del nombre a buscar.
     * @param activeOnly   `true` para buscar solo entre servicios activos, `false` para buscar entre todos.
     * @return Una lista de servicios que coinciden con el criterio de búsqueda.
     */
    List<OfferedService> findServicesByNameContaining(String nameFragment, boolean activeOnly);

}
