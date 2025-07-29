package infrastructure.adapter.out.persistence.mapper;

import domain.model.Booking;
import infrastructure.adapter.out.persistence.entity.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

// Le indicamos que ignore 'timeSlot.bookings' para romper el ciclo infinito (TimeSlot -> Booking -> TimeSlot).
@Mapper(componentModel = "spring", uses = {TimeSlotMapper.class, UserMapper.class})
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    BookingEntity toEntity(Booking booking);

    @Mapping(target = "timeSlot.bookings", ignore = true)
    Booking toDomain(BookingEntity bookingEntity);

    List<Booking> toDomainList(List<BookingEntity> bookingEntities);
}