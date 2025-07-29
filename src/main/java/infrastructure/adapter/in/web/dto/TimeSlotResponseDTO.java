package infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponseDTO {

    private String timeSlotUuid;
    private String serviceUuid;
    private String serviceName;
    private String providerUuid;

    private Instant startTime;
    private Instant endTime;

    private BigDecimal price;
    private String status; // e.g., "AVAILABLE", "FULL", "CANCELLED"

    private int capacity;
    private int availableSlots; // Campo calculado (capacity - bookings.size())
}