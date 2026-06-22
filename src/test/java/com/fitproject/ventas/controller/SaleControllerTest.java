package com.fitproject.ventas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitproject.ventas.dto.SaleDTO;
import com.fitproject.ventas.dto.SaleRequestDTO;
import com.fitproject.ventas.service.VentasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias de la capa HTTP para {@link SaleController}.
 */
@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @Mock private VentasService ventasService;

    @InjectMocks
    private SaleController saleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SaleDTO sampleSale;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(saleController).build();
        objectMapper = new ObjectMapper();
        sampleSale = SaleDTO.builder()
                .saleId("sale-1")
                .unitId("unit-1")
                .modelName("FitBox Basic")
                .buyerName("Carlos Comprador")
                .buyerEmail("carlos@example.com")
                .salePrice(new BigDecimal("25000.00"))
                .status("CONFIRMED")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/sales retorna 201 Created con la venta generada")
    void createSale_returns201WithSaleDTO() throws Exception {
        SaleRequestDTO req = new SaleRequestDTO();
        req.setUnitId("unit-1");
        req.setBuyerName("Carlos Comprador");
        req.setBuyerEmail("carlos@example.com");

        when(ventasService.createSale(any(SaleRequestDTO.class))).thenReturn(sampleSale);

        mockMvc.perform(post("/api/v1/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleId").value("sale-1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.modelName").value("FitBox Basic"));
    }

    @Test
    @DisplayName("GET /api/v1/sales retorna 200 con lista paginada de ventas")
    void getAllSales_returns200WithList() throws Exception {
        when(ventasService.getAllSales(0, 20)).thenReturn(List.of(sampleSale));

        mockMvc.perform(get("/api/v1/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saleId").value("sale-1"))
                .andExpect(jsonPath("$[0].buyerEmail").value("carlos@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/sales?page=1&size=5 usa parámetros de paginación correctamente")
    void getAllSales_withPagination_callsServiceWithCorrectParams() throws Exception {
        when(ventasService.getAllSales(1, 5)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/sales").param("page", "1").param("size", "5"))
                .andExpect(status().isOk());

        verify(ventasService).getAllSales(1, 5);
    }

    @Test
    @DisplayName("GET /api/v1/sales/buyer/{buyerId} retorna 200 con ventas del comprador")
    void getSalesByBuyer_returns200WithBuyerSales() throws Exception {
        when(ventasService.getSalesByBuyer("buyer-uuid")).thenReturn(List.of(sampleSale));

        mockMvc.perform(get("/api/v1/sales/buyer/buyer-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].buyerName").value("Carlos Comprador"));
    }
}
