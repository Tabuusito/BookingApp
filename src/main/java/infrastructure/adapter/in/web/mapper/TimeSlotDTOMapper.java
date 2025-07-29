package infrastructure.adapter.in.web.mapper;

import domain.model.TimeSlot;
import domain.model.TimeSlotStatus;
import infrastructure.adapter.in.web.dto.CreateTimeSlotRequestDTO;
import infrastructure.adapter.in.web.dto.TimeSlotResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TimeSlotDTOMapper {

    TimeSlotDTOMapper INSTANCE = Mappers.getMapper(TimeSlotDTOMapper.class);

    /**
     * Convierte un DTO de creación a un objeto de dominio TimeSlot.
     * Los campos como 'offeredService' o 'status' serán asignados por el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "offeredService", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    TimeSlot fromRequestDTO(CreateTimeSlotRequestDTO dto);

    /**
     * Convierte un objeto de dominio TimeSlot a un DTO de respuesta.
     */
    @Mapping(source = "uuid", target = "timeSlotUuid")
    @Mapping(source = "offeredService.uuid", target = "serviceUuid")
    @Mapping(source = "offeredService.name", target = "serviceName")
    @Mapping(source = "offeredService.owner.uuid", target = "providerUuid")
    @Mapping(source = "status", target = "status", qualifiedByName = "timeSlotStatusToString")
    @Mapping(target = "availableSlots", expression = "java(timeSlot.getCapacity() - timeSlot.getBookedCount())")
    TimeSlotResponseDTO toResponseDTO(TimeSlot timeSlot);

    @Named("timeSlotStatusToString")
    default String timeSlotStatusToString(TimeSlotStatus status) {
        return (status == null) ? null : status.name();
    }
}