package infrastructure.adapter.out.persistence.repository;

import domain.model.OfferedService;
import domain.port.out.OfferedServicePersistencePort;
import infrastructure.adapter.out.persistence.entity.OfferedServiceEntity;
import infrastructure.adapter.out.persistence.mapper.OfferedServiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OfferedServicePersistenceAdapter implements OfferedServicePersistencePort {

    private final OfferedServiceJpaRepository offeredServiceJpaRepository;
    private final OfferedServiceMapper offeredServiceMapper;

    @Override
    public OfferedService save(OfferedService offeredService) {
        OfferedServiceEntity entity = offeredServiceMapper.toEntity(offeredService);
        OfferedServiceEntity savedEntity = offeredServiceJpaRepository.save(entity);
        return offeredServiceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OfferedService> findByUuid(UUID serviceUuid) {
        return offeredServiceJpaRepository.findByUuid(serviceUuid)
                .map(offeredServiceMapper::toDomain);
    }

    @Override
    public void deleteByUuid(UUID serviceUuid) {
        offeredServiceJpaRepository.deleteByUuid(serviceUuid);

    }

    @Override
    public List<OfferedService> findAll() {
        return offeredServiceJpaRepository.findAll().stream()
                .map(offeredServiceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OfferedService> findAllActive() {
        return offeredServiceJpaRepository.findByIsActiveTrue().stream()
                .map(offeredServiceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OfferedService> findByNameContaining(String name) {
        return offeredServiceJpaRepository.findByNameContainingIgnoreCase(name).stream()
                .map(offeredServiceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return offeredServiceJpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Optional<OfferedService> updateActiveStatus(UUID serviceUuid, boolean isActive) {
        int updatedRows = offeredServiceJpaRepository.updateActiveStatus(serviceUuid, isActive);
        if (updatedRows > 0) {
            return this.findByUuid(serviceUuid);
        }
        return Optional.empty();
    }

    /**
     * Busca servicios cuyo nombre contenga nameFragment filtrando por isActive si procede.
     * Si activeOnly es false, busca en todos los servicios.
     * Si activeOnly es true, busca solo en los activos.
     *
     * @param nameFragment el nombre parcial o completo del servicio a buscar.
     * @param activeOnly   filtro para seleccionar todos o sólo los activos.
     * @return una lista de servicios.
     */
    @Override
    public List<OfferedService> findByNameContainingAndIsActive(String nameFragment, boolean activeOnly) {
        List<OfferedServiceEntity> entities;
        if (activeOnly) {
            entities = offeredServiceJpaRepository.findByNameContainingIgnoreCaseAndIsActive(nameFragment, true);
        } else {
            entities = offeredServiceJpaRepository.findByNameContainingIgnoreCase(nameFragment);
        }

        return offeredServiceMapper.toDomainList(entities);
    }

    @Override
    public boolean existsByNameAndOwnerId(String name, Long ownerId) {
        return offeredServiceJpaRepository.existsByNameIgnoreCaseAndOwnerId(name, ownerId);
    }

    @Override
    public List<OfferedService> findByOwnerIdAndIsActive(Long ownerId, boolean isActive) {
        List<OfferedServiceEntity> entities = offeredServiceJpaRepository.findByOwnerIdAndIsActive(ownerId, isActive);
        return offeredServiceMapper.toDomainList(entities);
    }

    @Override
    public List<OfferedService> findByOwnerId(Long ownerId) {
        List<OfferedServiceEntity> entities = offeredServiceJpaRepository.findByOwnerId(ownerId);
        return offeredServiceMapper.toDomainList(entities);
    }

    @Override
    public List<OfferedService> findByNameContainingAndOwnerIdAndIsActive(String nameFragment, Long ownerId, boolean isActive) {
        List<OfferedServiceEntity> entities = offeredServiceJpaRepository.findByNameContainingIgnoreCaseAndOwnerIdAndIsActive(nameFragment, ownerId, isActive);
        return offeredServiceMapper.toDomainList(entities);
    }
}
