package com.fitproject.ventas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaleRequestDTO {
    @NotBlank private String unitId;
    @NotBlank private String buyerName;
    @Email @NotBlank  private String buyerEmail;
    private String buyerId;
}
