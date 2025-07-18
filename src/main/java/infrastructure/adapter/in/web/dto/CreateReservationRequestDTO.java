package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReservationRequestDTO {

    private Long ownerId;

    @NotNull(message = "Service ID cannot be null")
    private Long serviceId;

    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be now or in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time cannot be null")
    @FutureOrPresent(message = "End time must be now or in the future")
    private LocalDateTime endTime;

    private String notes;

}
