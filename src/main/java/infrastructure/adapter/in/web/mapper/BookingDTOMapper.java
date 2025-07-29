package infrastructure.adapter.in.web.mapper;

import domain.model.Booking;
import domain.model.BookingStatus;
import infrastructure.adapter.in.web.dto.CreateBookingRequestDTO;
import infrastructure.adapter.in.web.dto.BookingResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingDTOMapper {

    BookingDTOMapper INSTANCE = Mappers.getMapper(BookingDTOMapper.class);

    /**
     * Convierte un DTO de creación a un objeto de dominio Booking.
     * La mayoría de los campos serán asignados por el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "timeSlot", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "pricePaid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Booking fromRequestDTO(CreateBookingRequestDTO dto);

    /**
     * Convierte un objeto de dominio Booking a un DTO de respuesta.
     */
    @Mapping(source = "uuid", target = "bookingUuid")
    @Mapping(source = "status", target = "status", qualifiedByName = "bookingStatusToString")
    @Mapping(source = "client.uuid", target = "clientUuid")
    @Mapping(source = "client.username", target = "clientUsername")
    @Mapping(source = "timeSlot.uuid", target = "timeSlotUuid")
    @Mapping(source = "timeSlot.startTime", target = "startTime")
    @Mapping(source = "timeSlot.endTime", target = "endTime")
    @Mapping(source = "timeSlot.offeredService.uuid", target = "serviceUuid")
    @Mapping(source = "timeSlot.offeredService.name", target = "serviceName")
    BookingResponseDTO toResponseDTO(Booking booking);

    @Named("bookingStatusToString")
    default String bookingStatusToString(BookingStatus status) {
        return (status == null) ? null : status.name();
    }
}