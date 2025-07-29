package infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferedServiceResponseDTO {

    private String serviceUuid;
    private String ownerUuid;
    private String name;
    private String description;

    @JsonProperty("defaultDurationSeconds")
    private Long defaultDurationSeconds;

    private BigDecimal pricePerReservation;
    private int capacity;
    private boolean isActive;
}
