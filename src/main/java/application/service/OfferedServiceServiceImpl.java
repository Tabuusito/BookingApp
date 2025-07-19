package application.service;


import domain.exception.DuplicateServiceNameException;
import domain.exception.OfferedServiceNotFoundException;
import domain.exception.ServiceInUseException;
import domain.exception.UserNotFoundException;
import domain.model.OfferedService;
import domain.model.User;
import domain.port.in.OfferedServiceService;
import domain.port.out.OfferedServicePersistencePort;
import domain.port.out.ReservationPersistencePort;
import domain.port.out.UserPersistencePort;
import infrastructure.adapter.in.web.security.RequesterContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferedServiceServiceImpl implements OfferedServiceService {

    private final OfferedServicePersistencePort offeredServicePersistencePort;
    private final ReservationPersistencePort reservationPersistencePort;
    private final UserPersistencePort userPersistencePort;


    private User getOwnerUserById(Long userId) {
        return userPersistencePort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Owner user with ID " + userId + " not found."));
    }

    // --- Implementaciones de los métodos del servicio ---

    @Override
    public OfferedService createOfferedService(OfferedService offeredService, Long ownerId, RequesterContext requester) {
        if (!requester.isAdmin() && !requester.isOwner(ownerId)) {
            throw new AccessDeniedException("You do not have permission to create a service for user ID " + ownerId + ".");
        }

        User owner;
        if(requester.userId().isPresent() && requester.userId().get().equals(ownerId)){
            owner = getOwnerUserById(ownerId);
        }
        else owner = offeredService.getOwner();

        offeredService.setOwner(owner);

        if (offeredServicePersistencePort.existsByNameAndOwnerId(offeredService.getName(), ownerId)) {
            throw new DuplicateServiceNameException("A service with the name '" + offeredService.getName() + "' already exists for this owner.");
        }
        // Si no se especifica si está activo, por defecto a TRUE
        if (offeredService.getIsActive() == null) {
            offeredService.setIsActive(Boolean.TRUE);
        }

        return offeredServicePersistencePort.save(offeredService);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OfferedService> findOfferedServiceByUuid(UUID serviceUuid, RequesterContext requester) {
        return offeredServicePersistencePort.findByUuid(serviceUuid)
                .filter(service -> {
                    // Autorización: Admin puede ver cualquier servicio, el dueño puede ver su servicio.
                    return requester.isAdmin() || requester.isOwner(service.getOwner().getId());
                });
    }

    @Override
    public Optional<OfferedService> updateOfferedService(UUID serviceUuid, OfferedService updateData, RequesterContext requester) {
        return offeredServicePersistencePort.findByUuid(serviceUuid).map(existingService -> {
            if (!requester.isAdmin() && !requester.isOwner(existingService.getOwner().getId())) {
                throw new AccessDeniedException("You do not have permission to update this service.");
            }

            if (updateData.getName() != null && !updateData.getName().equalsIgnoreCase(existingService.getName())) {
                if (offeredServicePersistencePort.existsByNameAndOwnerId(updateData.getName(), existingService.getOwner().getId())) {
                    throw new DuplicateServiceNameException("A service with the name '" + updateData.getName() + "' already exists for this owner.");
                }
                existingService.setName(updateData.getName());
            }

            // Aplicar otros cambios (comportamiento PATCH)
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

            return offeredServicePersistencePort.save(existingService);
        });
    }

    @Override
    public boolean deleteOfferedService(UUID serviceUuid, RequesterContext requester) {
        OfferedService serviceToDelete = offeredServicePersistencePort.findByUuid(serviceUuid)
                .orElseThrow(() -> new OfferedServiceNotFoundException("Service with UUID " + serviceUuid + " not found."));

        if (!requester.isAdmin() && !requester.isOwner(serviceToDelete.getOwner().getId())) {
            throw new AccessDeniedException("You do not have permission to delete this service.");
        }

        if (!reservationPersistencePort.findFutureReservationsByOfferedServiceUuid(serviceUuid).isEmpty()) {
            throw new ServiceInUseException("Cannot delete service with UUID " + serviceUuid + " because it has associated future reservations.");
        }

        offeredServicePersistencePort.deleteByUuid(serviceUuid);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferedService> findAllServices(RequesterContext requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only administrators can view all services in the system.");
        }
        return offeredServicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferedService> findAllActiveServices(RequesterContext requester) {
        if (requester.isAdmin()) {
            return offeredServicePersistencePort.findAllActive();
        } else if (requester.userId().isPresent()) {
            return offeredServicePersistencePort.findByOwnerIdAndIsActive(requester.userId().get(), true);
        } else {
            throw new AccessDeniedException("Authentication is required to view services.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferedService> findServicesByNameContaining(String nameFragment, boolean activeOnly, RequesterContext requester) {
        if (requester.isAdmin()) {
            return offeredServicePersistencePort.findByNameContainingAndIsActive(nameFragment, activeOnly);
        } else if (requester.userId().isPresent()) {
            return offeredServicePersistencePort.findByNameContainingAndOwnerIdAndIsActive(nameFragment, requester.userId().get(), activeOnly);
        } else {
            throw new AccessDeniedException("Authentication is required to search for services by name.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferedService> findMyServices(String nameContains, boolean activeOnly, RequesterContext requester) {
        Long myUserId = requester.userId()
                .orElseThrow(() -> new AccessDeniedException("User must be authenticated to view their own services."));

        if (nameContains != null && !nameContains.isBlank()) {
            return offeredServicePersistencePort.findByNameContainingAndOwnerIdAndIsActive(nameContains, myUserId, activeOnly);
        } else if (activeOnly) {
            return offeredServicePersistencePort.findByOwnerIdAndIsActive(myUserId, true);
        } else {
            return offeredServicePersistencePort.findByOwnerId(myUserId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferedService> findAllServicesForAdmin(String nameContains, boolean activeOnly, Optional<Long> ownerId, RequesterContext requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only administrators can use the admin service listing.");
        }

        boolean hasNameFilter = nameContains != null && !nameContains.isBlank();

        if (ownerId.isPresent()) {
            Long actualOwnerId = ownerId.get();
            getOwnerUserById(actualOwnerId);

            if (hasNameFilter) {
                return offeredServicePersistencePort.findByNameContainingAndOwnerIdAndIsActive(nameContains, actualOwnerId, activeOnly);
            } else {
                return offeredServicePersistencePort.findByOwnerIdAndIsActive(actualOwnerId, activeOnly);
            }
        } else {
            if (hasNameFilter) {
                return offeredServicePersistencePort.findByNameContainingAndIsActive(nameContains, activeOnly);
            } else {
                return offeredServicePersistencePort.findAllActive();
            }
        }
    }
}