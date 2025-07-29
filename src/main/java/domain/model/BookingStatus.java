package domain.model;

public enum BookingStatus {
    PENDING_PAYMENT,        // Esperando el pago del cliente
    AWAITING_CONFIRMATION,  // Pagado, esperando confirmación del proveedor (si es necesario)
    CONFIRMED,              // Plaza reservada y confirmada
    CANCELLED_BY_CLIENT,    // Cancelado por el cliente
    CANCELLED_BY_PROVIDER,  // Cancelado porque el proveedor canceló el TimeSlot
    COMPLETED,              // El cliente asistió
    NO_SHOW                 // El cliente no se presentó
}
