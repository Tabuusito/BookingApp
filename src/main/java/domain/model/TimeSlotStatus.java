package domain.model;

public enum TimeSlotStatus {
    AVAILABLE,  // Hay plazas libres
    FULL,       // Todas las plazas están ocupadas
    CANCELLED   // El proveedor ha cancelado el slot
}
