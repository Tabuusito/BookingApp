package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReservationRequestDTO {

    private Long ownerId;

    @NotNull(message = "Service UUID cannot be null")
    @UUID
    private String serviceUuid;

    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be now or in the future")
    private Instant startTime;

    @NotNull(message = "End time cannot be null")
    @FutureOrPresent(message = "End time must be now or in the future")
    private Instant endTime;

    private String notes;

}
