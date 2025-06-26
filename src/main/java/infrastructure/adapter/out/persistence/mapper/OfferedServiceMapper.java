package infrastructure.adapter.out.persistence.mapper;

import domain.model.OfferedService;
import infrastructure.adapter.out.persistence.entity.OfferedServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OfferedServiceMapper {

    OfferedServiceMapper INSTANCE = Mappers.getMapper(OfferedServiceMapper.class);


    OfferedServiceEntity toEntity(OfferedService offeredService);

    OfferedService toDomain(OfferedServiceEntity offeredServiceEntity);

    List<OfferedService> toDomainList(List<OfferedServiceEntity> offeredServiceEntities);
    List<OfferedServiceEntity> toEntityList(List<OfferedService> offeredServices);
}
