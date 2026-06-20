package com.fitproject.ventas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sale {
    @Id
    @Column(name = "sale_id")
    private String saleId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private GymUnit unit;
    @Column(name = "buyer_id")
    private String buyerId;
    @Column(name = "buyer_name")
    private String buyerName;
    @Column(name = "buyer_email")
    private String buyerEmail;
    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SaleStatus status = SaleStatus.PENDING;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        if (saleId == null) saleId = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
    }
}
