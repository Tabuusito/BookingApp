package infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "offered_services")
public class OfferedServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @Column(name = "public_uuid", unique = true, nullable = false, length = 36, updatable = false)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name="owner_id", nullable = false)
    private UserEntity owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, name = "default_duration_seconds")
    private Duration defaultDuration;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerReservation;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Boolean isActive;


    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }
}
