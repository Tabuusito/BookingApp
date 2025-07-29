package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTimeSlotRequestDTO {

    @NotNull(message = "Service UUID cannot be null")
    @UUID(message = "Service UUID must be a valid UUID")
    private String serviceUuid;

    @NotNull(message = "Start time cannot be null")
    @Future(message = "Start time must be in the future")
    private Instant startTime;

    @NotNull(message = "End time cannot be null")
    @Future(message = "End time must be in the future")
    private Instant endTime;

    // Opcional: si es nulo, se hereda del OfferedService
    @Min(value = 1, message = "Capacity must be at least 1, if provided")
    private Integer capacity;

    // Opcional: si es nulo, se hereda del OfferedService
    private BigDecimal price;
}