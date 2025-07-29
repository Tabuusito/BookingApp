package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequestDTO {

    @NotNull(message = "TimeSlot UUID cannot be null")
    @UUID(message = "TimeSlot UUID must be a valid UUID")
    private String timeSlotUuid;

    private String notes;
}