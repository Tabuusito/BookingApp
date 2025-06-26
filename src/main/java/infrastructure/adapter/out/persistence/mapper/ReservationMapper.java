package infrastructure.adapter.out.persistence.mapper;

import domain.model.Reservation;
import infrastructure.adapter.out.persistence.entity.ReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = { UserMapper.class, OfferedServiceMapper.class })
public interface ReservationMapper {

    ReservationMapper INSTANCE = Mappers.getMapper(ReservationMapper.class);


    ReservationEntity toEntity(Reservation reservation);

    Reservation toDomain(ReservationEntity reservationEntity);

    List<Reservation> toDomainList(List<ReservationEntity> reservationEntities);
    List<ReservationEntity> toEntityList(List<Reservation> reservations);
}
