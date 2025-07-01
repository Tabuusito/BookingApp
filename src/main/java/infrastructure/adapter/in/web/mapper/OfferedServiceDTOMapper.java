package infrastructure.adapter.in.web.mapper;

import domain.model.OfferedService;
import infrastructure.adapter.in.web.dto.CreateOfferedServiceRequestDTO;
import infrastructure.adapter.in.web.dto.OfferedServiceResponseDTO;
import infrastructure.adapter.in.web.dto.UpdateOfferedServiceRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Duration;

@Mapper(componentModel = "spring")
public interface OfferedServiceDTOMapper {

    OfferedServiceDTOMapper INSTANCE = Mappers.getMapper(OfferedServiceDTOMapper.class);

    @Mapping(target = "serviceId", ignore = true)
    @Mapping(source = "defaultDurationSeconds", target = "defaultDuration", qualifiedByName = "secondsToDuration")
    OfferedService fromRequestDTO(CreateOfferedServiceRequestDTO dto);

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "defaultDuration", target = "defaultDurationSeconds", qualifiedByName = "durationToSeconds")
    OfferedServiceResponseDTO toResponseDTO(OfferedService offeredService);

    @Mapping(target = "serviceId", ignore = true)
    @Mapping(source = "defaultDurationSeconds", target = "defaultDuration", qualifiedByName = "secondsToDuration")
    OfferedService fromRequestDTO(UpdateOfferedServiceRequestDTO dto);


    @Named("durationToSeconds")
    default Long durationToSeconds(Duration duration) {
        return duration == null ? null : duration.getSeconds();
    }

    @Named("secondsToDuration")
    default Duration secondsToDuration(Long seconds) {
        return seconds == null ? null : Duration.ofSeconds(seconds);
    }
}
