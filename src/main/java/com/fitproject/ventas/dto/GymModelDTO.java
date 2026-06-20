package com.fitproject.ventas.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GymModelDTO {
    private String modelId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer areaM2;
    private Integer capacity;
    private String imageUrl;
    private Boolean available;
}
