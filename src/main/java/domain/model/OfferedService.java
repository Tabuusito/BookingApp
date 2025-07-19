package domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferedService {
    private Long serviceId;
    private UUID uuid;
    private User owner;
    private String name;
    private String description;
    private Duration defaultDuration;
    private BigDecimal pricePerReservation;
    private Boolean isActive;

}