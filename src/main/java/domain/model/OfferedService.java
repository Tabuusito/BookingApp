package domain.model;

import java.math.BigDecimal;
import java.time.Duration;

public class OfferedService {
    private Long serviceId;
    private String name;
    private String description;
    private Duration defaultDuration;
    private BigDecimal pricePerReservation;
    private boolean isActive;

}