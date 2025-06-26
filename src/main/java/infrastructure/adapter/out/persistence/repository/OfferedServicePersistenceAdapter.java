package infrastructure.adapter.out.persistence.repository;

import domain.model.OfferedService;
import domain.port.out.OfferedServicePersistencePort;
import infrastructure.adapter.out.persistence.entity.OfferedServiceEntity;
import infrastructure.adapter.out.persistence.mapper.OfferedServiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
    public Optional<OfferedService> findById(Long serviceId) {
        return offeredServiceJpaRepository.findById(serviceId)
                .map(offeredServiceMapper::toDomain);
    }

    @Override
    public void deleteById(Long serviceId) {
        offeredServiceJpaRepository.deleteById(serviceId);

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
    public Optional<OfferedService> updateActiveStatus(Long serviceId, boolean isActive) {
        int updatedRows = offeredServiceJpaRepository.updateActiveStatus(serviceId, isActive);
        if (updatedRows > 0) {
            return this.findById(serviceId);
        }
        return Optional.empty();
    }
}
