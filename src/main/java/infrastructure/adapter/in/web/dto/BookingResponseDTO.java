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
public class BookingResponseDTO {

    private String bookingUuid;
    private String status; // "CONFIRMED", "CANCELLED_BY_CLIENT", etc.

    // Información del cliente que reservó
    private String clientUuid;
    private String clientUsername;

    // Información del slot reservado
    private String timeSlotUuid;
    private Instant startTime;
    private Instant endTime;

    // Información del servicio asociado
    private String serviceUuid;
    private String serviceName;

    private BigDecimal pricePaid;
    private String notes;

    private Instant createdAt;
    private Instant updatedAt;
}