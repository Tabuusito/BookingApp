package infrastructure.adapter.out.persistence.mapper;

import domain.model.TimeSlot;
import infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OfferedServiceMapper.class})
public interface TimeSlotMapper {

    TimeSlotMapper INSTANCE = Mappers.getMapper(TimeSlotMapper.class);

    @Mapping(target = "bookings", ignore = true)
    TimeSlotEntity toEntity(TimeSlot timeSlot);

    @Mapping(target = "bookings", ignore = true)
    TimeSlot toDomain(TimeSlotEntity timeSlotEntity);

    List<TimeSlot> toDomainList(List<TimeSlotEntity> timeSlotEntities);
}