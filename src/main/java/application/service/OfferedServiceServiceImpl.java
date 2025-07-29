package application.service;

import domain.exception.DuplicateServiceNameException;
import domain.exception.OfferedServiceNotFoundException;
import domain.exception.ServiceInUseException;
import domain.exception.UserNotFoundException;
import domain.model.OfferedService;
import domain.model.User;
import domain.port.in.OfferedServiceService;
import domain.port.out.OfferedServicePersistencePort;
import domain.port.out.TimeSlotPersistencePort; // <-- NUEVA DEPENDENCIA
import domain.port.out.UserPersistencePort;
import infrastructure.adapter.in.web.security.SpringSecurityUser; // <-- NUEVO IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder; // <-- NUEVO IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant; // <-- NUEVO IMPORT
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferedServiceServiceImpl implements OfferedServiceService {

    private final OfferedServicePersistencePort offeredServicePersistencePort;
    private final TimeSlotPersistencePort timeSlotPersistencePort; // <-- REEMPLAZA A RESERVATION PORT
    private final UserPersistencePort userPersistencePort;

    // Método de ayuda ahora busca por UUID para consistencia
    private User getOwnerUserByUuid(UUID userUuid) {
        return userPersistencePort.findByUuid(userUuid)
                .orElseThrow(() -> new UserNotFoundException("Owner user with UUID " + userUuid + " not found."));
    }

    // --- Implementaciones Refactorizadas ---

    @Override
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isSelf(authentication, #ownerUuid)")
    public OfferedService createOfferedService(OfferedService offeredService, UUID ownerUuid) {
        User owner = getOwnerUserByUuid(ownerUuid);
        offeredService.setOwner(owner);

        if (offeredServicePersistencePort.existsByNameAndOwnerId(offeredService.getName(), owner.getId())) {
            throw new DuplicateServiceNameException("A service with the name '" + offeredService.getName() + "' already exists for this owner.");
        }

        if (offeredService.getIsActive() == null) {
            offeredService.setIsActive(Boolean.TRUE);
        }

        return offeredServicePersistencePort.save(offeredService);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isServiceOwner(#serviceUuid)")
    public Optional<OfferedService> findOfferedServiceByUuid(UUID serviceUuid) {
        return offeredServicePersistencePort.findByUuid(serviceUuid);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isServiceOwner(#serviceUuid)")
    public Optional<OfferedService> updateOfferedService(UUID serviceUuid, OfferedService updateData) {
        return offeredServicePersistencePort.findByUuid(serviceUuid).map(existingService -> {

            if (updateData.getName() != null && !updateData.getName().equalsIgnoreCase(existingService.getName())) {
                if (offeredServicePersistencePort.existsByNameAndOwnerId(updateData.getName(), existingService.getOwner().getId())) {
                    throw new DuplicateServiceNameException("A service with the name '" + updateData.getName() + "' already exists for this owner.");
                }
                existingService.setName(updateData.getName());
            }

            // Aplicar otros cambios (PATCH)
            if (updateData.getDescription() != null) {
                existingService.setDescription(updateData.getDescription());
            }
            if (updateData.getDefaultDuration() != null) {
                existingService.setDefaultDuration(updateData.getDefaultDuration());
            }
            if (updateData.getPricePerReservation() != null) {
                existingService.setPricePerReservation(updateData.getPricePerReservation());
            }
            if (updateData.getIsActive() != null) {
                existingService.setIsActive(updateData.getIsActive());
            }
            if (updateData.getCapacity() != null) {
                existingService.setCapacity(updateData.getCapacity());
            }

            return offeredServicePersistencePort.save(existingService);
        });
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isServiceOwner(#serviceUuid)")
    public void deleteOfferedService(UUID serviceUuid) {
        // La autorización ya se ha realizado. Si el servicio no existe, findByUuid devolverá Optional.empty()
        // y el chequeo de abajo lanzará la excepción correcta.
        if (!offeredServicePersistencePort.findByUuid(serviceUuid).isPresent()) {
            throw new OfferedServiceNotFoundException("Service with UUID " + serviceUuid + " not found.");
        }

        // LÓGICA DE NEGOCIO ACTUALIZADA:
        // No se puede borrar un servicio si tiene TimeSlots futuros asociados.
        // Asumimos un nuevo método en el puerto de TimeSlot.
        if (timeSlotPersistencePort.hasFutureTimeSlots(serviceUuid)) {
            throw new ServiceInUseException("Cannot delete service with UUID " + serviceUuid + " because it has associated future time slots.");
        }

        offeredServicePersistencePort.deleteByUuid(serviceUuid);
    }

    // NOTA: Para implementar `timeSlotPersistencePort.hasFutureTimeSlots(serviceUuid)`,
    // necesitarás un nuevo método en el puerto y su implementación:
    // En TimeSlotPersistencePort: boolean hasFutureTimeSlots(UUID serviceUuid);
    // En TimeSlotJpaRepository: boolean existsByOfferedService_UuidAndStartTimeAfter(UUID serviceUuid, Instant now);

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<OfferedService> findAllServices() {
        return offeredServicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<OfferedService> findAllActiveServices() {
        return offeredServicePersistencePort.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<OfferedService> findMyServices(String nameContains, boolean activeOnly) {
        UUID myUserUuid = ((SpringSecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUuid();
        User owner = getOwnerUserByUuid(myUserUuid);

        // El resto de la lógica puede permanecer, pero ahora usa el ID del owner obtenido.
        if (nameContains != null && !nameContains.isBlank()) {
            return offeredServicePersistencePort.findByNameContainingAndOwnerIdAndIsActive(nameContains, owner.getId(), activeOnly);
        } else if (activeOnly) {
            return offeredServicePersistencePort.findByOwnerIdAndIsActive(owner.getId(), true);
        } else {
            return offeredServicePersistencePort.findByOwnerId(owner.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<OfferedService> findAllServicesForAdmin(String nameContains, Optional<UUID> ownerUuid, boolean activeOnly) {
        boolean hasNameFilter = nameContains != null && !nameContains.isBlank();

        if (ownerUuid.isPresent()) {
            User owner = getOwnerUserByUuid(ownerUuid.get());
            Long actualOwnerId = owner.getId();

            if (hasNameFilter) {
                return offeredServicePersistencePort.findByNameContainingAndOwnerIdAndIsActive(nameContains, actualOwnerId, activeOnly);
            } else {
                return offeredServicePersistencePort.findByOwnerIdAndIsActive(actualOwnerId, activeOnly);
            }
        } else {
            if (hasNameFilter) {
                return offeredServicePersistencePort.findByNameContainingAndIsActive(nameContains, activeOnly);
            } else if (activeOnly) {
                return offeredServicePersistencePort.findAllActive();
            } else {
                return offeredServicePersistencePort.findAll();
            }
        }
    }

    // El método `findServicesByNameContaining` se vuelve redundante si `findAllServicesForAdmin`
    // puede manejar la búsqueda sin un ownerId. Podemos eliminarlo o mantenerlo si tiene un caso de uso específico.
    // El método `findAllActiveServices` también puede ser cubierto por los otros.
    // Simplificar es bueno.
}