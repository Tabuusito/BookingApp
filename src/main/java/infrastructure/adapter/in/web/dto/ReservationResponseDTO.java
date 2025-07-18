package infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponseDTO {

    private Long reservationId;

    private Long ownerId;

    private String ownerUsername;

    private Long serviceId;

    private String serviceName;

    private Instant startTime;

    private Instant endTime;

    private String status;

    private BigDecimal price;

    private String notes;

    private Instant createdAt;

    private Instant updatedAt;
}
