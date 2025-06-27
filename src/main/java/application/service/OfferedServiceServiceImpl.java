package application.service;


import domain.exception.DuplicateServiceNameException;
import domain.exception.ServiceInUseException;
import domain.model.OfferedService;
import domain.port.in.OfferedServiceService;
import domain.port.out.OfferedServicePersistencePort;
import domain.port.out.ReservationPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OfferedServiceServiceImpl implements OfferedServiceService {

    private final OfferedServicePersistencePort offeredServicePersistencePort;
    private final ReservationPersistencePort reservationPersistencePort; // Inyectar para lógica de negocio cruzada

    @Override
    public OfferedService createOfferedService(OfferedService offeredService) {
        // Lógica de negocio: Validar que no exista otro servicio con el mismo nombre (si es un requisito)
        if (offeredServicePersistencePort.existsByName(offeredService.getName())) {
            throw new DuplicateServiceNameException("A service with the name '" + offeredService.getName() + "' already exists.");
        }


        if (offeredService.getIsActive() == null) {
            offeredService.setIsActive(Boolean.TRUE);
        }

        return offeredServicePersistencePort.save(offeredService);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OfferedService> findOfferedServiceById(Long serviceId) {
        return offeredServicePersistencePort.findById(serviceId);
    }

    @Override
    public Optional<OfferedService> updateOfferedService(Long serviceId, OfferedService updateData) {
        return offeredServicePersistencePort.findById(serviceId).map(existingService -> {
            if (updateData.getName() != null && !updateData.getName().equalsIgnoreCase(existingService.getName())) {
                if (offeredServicePersistencePort.existsByName(updateData.getName())) {
                    throw new DuplicateServiceNameException("A service with the name '" + updateData.getName() + "' already exists.");
                }
                existingService.setName(updateData.getName());
            }

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
    public boolean deleteOfferedService(Long serviceId) {

        if (!reservationPersistencePort.findFutureReservationsByOfferedServiceId(serviceId).isEmpty()) {
            throw new ServiceInUseException("Cannot delete service with ID " + serviceId + " because it has associated future reservations.");
        }

        if (offeredServicePersistencePort.findById(serviceId).isPresent()) {
            offeredServicePersistencePort.deleteById(serviceId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferedService> findAllServices() {
        return offeredServicePersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferedService> findAllActiveServices() {
        return offeredServicePersistencePort.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferedService> findServicesByNameContaining(String nameFragment, boolean activeOnly) {
        if (activeOnly) {
            return offeredServicePersistencePort.findByNameContainingAndIsActive(nameFragment, activeOnly);
        } else {
            return offeredServicePersistencePort.findByNameContaining(nameFragment);
        }
    }
}
