package com.fitproject.ventas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_models")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GymModel {
    @Id
    @Column(name = "model_id")
    private String modelId;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(precision = 12, scale = 2)
    private BigDecimal price;
    private Integer areaM2;
    private Integer capacity;
    @Column(name = "image_url")
    private String imageUrl;
    @Builder.Default
    private Boolean available = true;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        if (modelId == null) modelId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
    }
}
