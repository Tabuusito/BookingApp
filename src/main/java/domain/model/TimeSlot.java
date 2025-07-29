package domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

    private Long id;
    private UUID uuid;

    // El servicio que se ofrece en este slot
    private OfferedService offeredService;

    // Horario específico del slot
    private Instant startTime;
    private Instant endTime;

    // Capacidad y precio para este slot específico (puede anular los valores por defecto del OfferedService)
    private Integer capacity;
    private BigDecimal price;

    // Estado del slot
    private TimeSlotStatus status;

    // Las reservas (bookings) asociadas a este slot
    @Builder.Default // Para que Lombok inicialice el set aunque no se especifique en el builder
    private Set<Booking> bookings = new HashSet<>();

    // --- Lógica de Dominio ---

    public int getBookedCount() {
        return bookings == null ? 0 : bookings.size();
    }

    public boolean isFull() {
        return getBookedCount() >= capacity;
    }
}
