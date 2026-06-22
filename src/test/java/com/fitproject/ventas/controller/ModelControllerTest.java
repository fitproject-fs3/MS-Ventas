package com.fitproject.ventas.controller;

import com.fitproject.ventas.dto.GymModelDTO;
import com.fitproject.ventas.service.VentasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias de la capa HTTP para {@link ModelController}.
 */
@ExtendWith(MockitoExtension.class)
class ModelControllerTest {

    @Mock private VentasService ventasService;

    @InjectMocks
    private ModelController modelController;

    private MockMvc mockMvc;
    private GymModelDTO availableModel;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(modelController).build();
        availableModel = GymModelDTO.builder()
                .modelId("model-1")
                .name("FitBox Basic")
                .description("Módulo básico")
                .price(new BigDecimal("25000.00"))
                .areaM2(20).capacity(15)
                .available(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/models retorna 200 con modelos disponibles")
    void getAvailable_returns200WithAvailableModels() throws Exception {
        when(ventasService.getAvailableModels(0, 20)).thenReturn(List.of(availableModel));

        mockMvc.perform(get("/api/v1/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].modelId").value("model-1"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/models retorna 200 con lista vacía si no hay modelos disponibles")
    void getAvailable_noModels_returns200EmptyList() throws Exception {
        when(ventasService.getAvailableModels(0, 20)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/models/all retorna 200 con catálogo completo")
    void getAll_returns200WithFullCatalog() throws Exception {
        GymModelDTO unavailable = GymModelDTO.builder()
                .modelId("model-2").name("FitBox Pro")
                .price(new BigDecimal("50000.00")).available(false).build();
        when(ventasService.getAllModels(0, 20)).thenReturn(List.of(availableModel, unavailable));

        mockMvc.perform(get("/api/v1/models/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("FitBox Pro"));
    }

    @Test
    @DisplayName("GET /api/v1/models?page=0&size=5 pasa parámetros de paginación al servicio")
    void getAvailable_withPagination_passesParamsToService() throws Exception {
        when(ventasService.getAvailableModels(0, 5)).thenReturn(List.of(availableModel));

        mockMvc.perform(get("/api/v1/models").param("page", "0").param("size", "5"))
                .andExpect(status().isOk());

        verify(ventasService).getAvailableModels(0, 5);
    }
}
