package domain.port.out;

import domain.model.OfferedService;
import domain.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface OfferedServicePersistencePort {

    /**
     * Guarda un nuevo servicio ofrecido o actualiza uno existente.
     * @param offeredService el servicio a guardar.
     * @return el servicio guardado (con el ID asignado si es nuevo).
     */
    OfferedService save(OfferedService offeredService);

    /**
     * Busca un servicio ofrecido por su ID.
     * @param serviceId el ID del servicio.
     * @return un Optional conteniendo el servicio si se encuentra, o un Optional vacío.
     */
    Optional<OfferedService> findById(Long serviceId);

    /**
     * Elimina un servicio ofrecido por su ID.
     * (Considerar la lógica de negocio: ¿qué pasa con las reservas existentes para este servicio?).
     * @param serviceId el ID del servicio a eliminar.
     */
    void deleteById(Long serviceId); // Podría devolver boolean para indicar si se eliminó

    /**
     * Obtiene todos los servicios ofrecidos.
     * (Considerar la paginación).
     * @return una lista de todos los servicios ofrecidos.
     */
    List<OfferedService> findAll();

    /**
     * Obtiene todos los servicios ofrecidos que están activos.
     * Útil para mostrar a los clientes solo los servicios disponibles.
     * @return una lista de todos los servicios activos.
     */
    List<OfferedService> findAllActive();

    /**
     * Busca un servicio ofrecido por su nombre.
     * (Asumir que el nombre podría no ser único, por eso devuelve una lista,
     * o si es único, podría devolver Optional<OfferedService>).
     * @param name el nombre del servicio.
     * @return una lista de servicios con ese nombre.
     */
    List<OfferedService> findByNameContaining(String name); // Búsqueda por nombre parcial

    /**
     * Verifica si existe un servicio con un nombre específico.
     * Útil para evitar duplicados si los nombres deben ser únicos.
     * @param name el nombre del servicio.
     * @return true si existe un servicio con ese nombre, false en caso contrario.
     */
    boolean existsByName(String name);

    // --- Métodos adicionales que podrían ser útiles ---

    /**
     * Cambia el estado de activación de un servicio.
     * @param serviceId el ID del servicio.
     * @param isActive el nuevo estado de activación.
     * @return el servicio actualizado, o Optional.empty() si no se encontró.
     */
    Optional<OfferedService> updateActiveStatus(Long serviceId, boolean isActive);

    /**
     * Busca servicios cuyo nombre
     * contenga nameFragment filtrando por isActive si procede
     * @param nameFragment el nombre parcial o completo del servicio a buscar.
     * @param activeOnly filtro para seleccionar todos o sólo los activos.
     * @return una lista de servicios.
     */
    List<OfferedService> findByNameContainingAndIsActive(String nameFragment, boolean activeOnly);

    boolean existsByNameAndOwnerId(String name, Long ownerId);

    List<OfferedService> findByOwnerIdAndIsActive(Long ownerId, boolean isActive);

    List<OfferedService> findByOwnerId(Long ownerId);

    List<OfferedService> findByNameContainingAndOwnerIdAndIsActive(String nameFragment, Long ownerId, boolean isActive);
}

