package domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    private Long id;
    private UUID uuid;

    // El slot de tiempo al que se ha apuntado el cliente
    private TimeSlot timeSlot;

    // El cliente que ha hecho la reserva
    private User client;

    // Estado del booking
    private BookingStatus status;

    // El precio que se pagó, por si hay descuentos o cambios
    private BigDecimal pricePaid;

    // Notas adicionales del cliente
    private String notes;

    private Instant createdAt;
    private Instant updatedAt;

    // --- Lógica de Dominio ---

    public void confirm() {
        if (this.status == BookingStatus.PENDING_PAYMENT || this.status == BookingStatus.AWAITING_CONFIRMATION) {
            this.status = BookingStatus.CONFIRMED;
            this.updatedAt = Instant.now();
        } else {
            throw new IllegalStateException("Booking cannot be confirmed from status: " + this.status);
        }
    }

    public void cancel() {
        if (this.status == BookingStatus.CONFIRMED || this.status == BookingStatus.PENDING_PAYMENT) {
            this.status = BookingStatus.CANCELLED_BY_CLIENT;
            this.updatedAt = Instant.now();
        } else {
            throw new IllegalStateException("Booking cannot be cancelled from status: " + this.status);
        }
    }
}
