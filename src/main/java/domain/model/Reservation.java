package domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    private Long reservationId;
    private User user;
    private OfferedService service;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private BigDecimal price;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void confirm() {
        if (this.status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.CONFIRMED;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Reservation cannot be confirmed from status: " + this.status);
        }
    }

    public void cancel() {
        if (this.status == ReservationStatus.PENDING || this.status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.CANCELLED;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Reservation cannot be cancelled from status: " + this.status);
        }
    }

    public boolean isOverlapping(LocalDateTime otherStartTime, LocalDateTime otherEndTime) {
        return this.startTime.isBefore(otherEndTime) && otherStartTime.isBefore(this.endTime);
    }
}
