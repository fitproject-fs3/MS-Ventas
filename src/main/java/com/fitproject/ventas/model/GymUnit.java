package com.fitproject.ventas.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_units")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GymUnit {
    @Id
    @Column(name = "unit_id")
    private String unitId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private GymModel model;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UnitStatus status = UnitStatus.AVAILABLE;
    @Column(name = "serial_number", unique = true)
    private String serialNumber;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        if (unitId == null) unitId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
    }
}
