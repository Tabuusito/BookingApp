package domain.model;

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
public class Reservation {

    private Long reservationId;
    private User owner;
    private OfferedService service;
    private Instant startTime;
    private Instant endTime;
    private ReservationStatus status;
    private BigDecimal price;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public void confirm() {
        if (this.status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.CONFIRMED;
            this.updatedAt = Instant.now();
        } else {
            throw new IllegalStateException("Reservation cannot be confirmed from status: " + this.status);
        }
    }

    public void cancel() {
        if (this.status == ReservationStatus.PENDING || this.status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.CANCELLED;
            this.updatedAt = Instant.now();
        } else {
            throw new IllegalStateException("Reservation cannot be cancelled from status: " + this.status);
        }
    }

    public boolean isOverlapping(Instant otherStartTime, Instant otherEndTime) {
        return this.startTime.isBefore(otherEndTime) && otherStartTime.isBefore(this.endTime);
    }
}
