package com.fitproject.ventas.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SaleDTO {
    private String saleId;
    private String unitId;
    private String modelName;
    private String buyerName;
    private String buyerEmail;
    private BigDecimal salePrice;
    private String status;
    private LocalDateTime createdAt;
}
