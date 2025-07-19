package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOfferedServiceRequestDTO {

    @Size(min = 3, max = 100, message = "Service name must be between 3 and 100 characters, if provided")
    private String name;

    private Long ownerId;

    private String description;

    @Min(value = 1, message = "Default duration must be at least 1 second, if provided")
    private Long defaultDurationSeconds;

    @DecimalMin(value = "0.0", message = "Price must be greater than 0, if provided")
    private BigDecimal pricePerReservation;

    private Boolean isActive;
}
