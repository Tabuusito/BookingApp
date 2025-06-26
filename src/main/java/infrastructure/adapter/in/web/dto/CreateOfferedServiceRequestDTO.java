package infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfferedServiceRequestDTO {

    @NotBlank(message = "Service name cannot be blank")
    @Size(min = 3, max = 100, message = "Service name must be between 3 and 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Default duration in seconds cannot be null")
    @Min(value = 1, message = "Default duration must be at least 1 second")
    private Long defaultDurationSeconds;

    @NotNull(message = "Price per reservation cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal pricePerReservation;

    private Boolean isActive;
}
