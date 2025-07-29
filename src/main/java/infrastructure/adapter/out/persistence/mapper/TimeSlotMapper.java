package infrastructure.adapter.out.persistence.mapper;

import domain.model.TimeSlot;
import infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OfferedServiceMapper.class, BookingMapper.class})
public interface TimeSlotMapper {

    TimeSlotMapper INSTANCE = Mappers.getMapper(TimeSlotMapper.class);

    // Mapeamos los bookings para el cálculo de plazas, pero los ignoramos al convertir
    // de dominio a entidad para evitar ciclos de guardado.
    @Mapping(target = "bookings", ignore = true)
    TimeSlotEntity toEntity(TimeSlot timeSlot);

    TimeSlot toDomain(TimeSlotEntity timeSlotEntity);

    List<TimeSlot> toDomainList(List<TimeSlotEntity> timeSlotEntities);
}