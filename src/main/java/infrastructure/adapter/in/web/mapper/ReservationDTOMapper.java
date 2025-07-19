package infrastructure.adapter.in.web.mapper;

import domain.model.Reservation;
import domain.model.ReservationStatus;
import infrastructure.adapter.in.web.dto.CreateReservationRequestDTO;
import infrastructure.adapter.in.web.dto.ReservationResponseDTO;
import infrastructure.adapter.in.web.dto.UpdateReservationRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface ReservationDTOMapper {

    ReservationDTOMapper INSTANCE = Mappers.getMapper(ReservationDTOMapper.class);

    @Mapping(source = "owner.uuid", target = "ownerUuid")
    @Mapping(source = "owner.username", target = "ownerUsername")
    @Mapping(source = "service.uuid", target = "serviceUuid")
    @Mapping(source = "service.name", target = "serviceName")
    @Mapping(source = "status", target = "status", qualifiedByName = "reservationStatusToString")
    ReservationResponseDTO toResponseDTO(Reservation reservation);

    @Mapping(target = "reservationId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reservation fromRequestDTO(CreateReservationRequestDTO dto);

    @Mapping(target = "reservationId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "status", qualifiedByName = "stringToReservationStatus")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reservation fromRequestDTO(UpdateReservationRequestDTO dto);


    @Named("reservationStatusToString")
    default String reservationStatusToString(ReservationStatus status) {
        return (status == null) ? null : status.name();
    }

    @Named("stringToReservationStatus")
    default ReservationStatus stringToReservationStatus(String statusString) {
        if (statusString == null || statusString.isBlank()) {
            return null;
        }
        try {
            return ReservationStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Error mapeando string a ReservationStatus: " + statusString + " - " + e.getMessage());
            return null;
        }
    }
}
