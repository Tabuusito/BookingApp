package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReservationRequestDTO {

    private Long serviceId;

    private Long ownerId;

    @FutureOrPresent(message = "Start time must be now or in the future if provided")
    private Instant startTime;

    @FutureOrPresent(message = "End time must be now or in the future if provided")
    private Instant endTime;

    private BigDecimal price;

    private String status;

    private String notes;

}
