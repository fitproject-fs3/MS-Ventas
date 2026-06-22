package com.fitproject.ventas.service;

import com.fitproject.ventas.config.RabbitMQConfig;
import com.fitproject.ventas.dto.GymModelDTO;
import com.fitproject.ventas.dto.SaleDTO;
import com.fitproject.ventas.dto.SaleRequestDTO;
import com.fitproject.ventas.model.*;
import com.fitproject.ventas.repository.GymModelRepository;
import com.fitproject.ventas.repository.GymUnitRepository;
import com.fitproject.ventas.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link VentasService}.
 * Cubre escenarios de éxito y manejo de excepciones para garantizar ≥60% de cobertura JaCoCo.
 */
@ExtendWith(MockitoExtension.class)
class VentasServiceTest {

    @Mock private GymModelRepository modelRepository;
    @Mock private GymUnitRepository unitRepository;
    @Mock private SaleRepository saleRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private VentasService ventasService;

    private GymModel gymModel;
    private GymUnit gymUnit;
    private Sale sale;

    @BeforeEach
    void setUp() {
        gymModel = GymModel.builder()
                .modelId("model-1")
                .name("FitBox Basic")
                .description("Módulo básico de 20m²")
                .price(new BigDecimal("25000.00"))
                .areaM2(20)
                .capacity(15)
                .imageUrl("https://cdn.fitproject.com/basic.jpg")
                .available(true)
                .build();

        gymUnit = GymUnit.builder()
                .unitId("unit-1")
                .model(gymModel)
                .status(UnitStatus.AVAILABLE)
                .serialNumber("FIT-2024-001")
                .build();

        sale = Sale.builder()
                .saleId("sale-1")
                .unit(gymUnit)
                .buyerId("buyer-uuid")
                .buyerName("Carlos Comprador")
                .buyerEmail("carlos@example.com")
                .salePrice(new BigDecimal("25000.00"))
                .status(SaleStatus.CONFIRMED)
                .build();
    }

    // ─── getAvailableModels ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAvailableModels: retorna modelos disponibles paginados")
    void getAvailableModels_returnsDTOList() {
        when(modelRepository.findByAvailableTrue(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(gymModel)));

        List<GymModelDTO> result = ventasService.getAvailableModels(0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelId()).isEqualTo("model-1");
        assertThat(result.get(0).getName()).isEqualTo("FitBox Basic");
        assertThat(result.get(0).getAvailable()).isTrue();
    }

    @Test
    @DisplayName("getAvailableModels: retorna lista vacía si no hay modelos disponibles")
    void getAvailableModels_noModels_returnsEmptyList() {
        when(modelRepository.findByAvailableTrue(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of()));

        List<GymModelDTO> result = ventasService.getAvailableModels(0, 10);

        assertThat(result).isEmpty();
    }

    // ─── getAllModels ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllModels: retorna catálogo completo paginado")
    void getAllModels_returnsDTOList() {
        GymModel unavailable = GymModel.builder()
                .modelId("model-2").name("FitBox Pro")
                .price(new BigDecimal("50000.00")).available(false).build();
        when(modelRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(gymModel, unavailable)));

        List<GymModelDTO> result = ventasService.getAllModels(0, 20);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GymModelDTO::getModelId)
                .containsExactlyInAnyOrder("model-1", "model-2");
    }

    // ─── createSale ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createSale: persiste la venta, marca unidad como SOLD y publica evento RabbitMQ")
    void createSale_success_persistsSaleAndPublishesEvent() {
        SaleRequestDTO req = new SaleRequestDTO();
        req.setUnitId("unit-1");
        req.setBuyerId("buyer-uuid");
        req.setBuyerName("Carlos Comprador");
        req.setBuyerEmail("carlos@example.com");

        when(unitRepository.findById("unit-1")).thenReturn(Optional.of(gymUnit));
        when(unitRepository.save(gymUnit)).thenReturn(gymUnit);
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        SaleDTO result = ventasService.createSale(req);

        assertThat(result).isNotNull();
        assertThat(result.getSaleId()).isEqualTo("sale-1");
        assertThat(result.getBuyerName()).isEqualTo("Carlos Comprador");
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(gymUnit.getStatus()).isEqualTo(UnitStatus.SOLD);
        verify(unitRepository).save(gymUnit);
        verify(saleRepository).save(any(Sale.class));
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATIONS_EXCHANGE),
                eq(RabbitMQConfig.SALE_CONFIRMED_KEY),
                any());
    }

    @Test
    @DisplayName("createSale: lanza IllegalArgumentException si la unidad no existe")
    void createSale_unitNotFound_throwsIllegalArgumentException() {
        SaleRequestDTO req = new SaleRequestDTO();
        req.setUnitId("unit-xxx");
        req.setBuyerName("Ana");
        req.setBuyerEmail("ana@example.com");

        when(unitRepository.findById("unit-xxx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventasService.createSale(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unidad no encontrada");

        verify(saleRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("createSale: lanza IllegalStateException si la unidad no está disponible")
    void createSale_unitNotAvailable_throwsIllegalStateException() {
        gymUnit.setStatus(UnitStatus.SOLD);
        SaleRequestDTO req = new SaleRequestDTO();
        req.setUnitId("unit-1");
        req.setBuyerName("Luis");
        req.setBuyerEmail("luis@example.com");

        when(unitRepository.findById("unit-1")).thenReturn(Optional.of(gymUnit));

        assertThatThrownBy(() -> ventasService.createSale(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está disponible");

        verify(saleRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    // ─── getAllSales ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllSales: retorna historial completo de ventas paginado")
    void getAllSales_returnsDTOList() {
        when(saleRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(sale)));

        List<SaleDTO> result = ventasService.getAllSales(0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSaleId()).isEqualTo("sale-1");
        assertThat(result.get(0).getModelName()).isEqualTo("FitBox Basic");
    }

    @Test
    @DisplayName("getAllSales: retorna lista vacía si no hay ventas registradas")
    void getAllSales_noSales_returnsEmptyList() {
        when(saleRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of()));

        List<SaleDTO> result = ventasService.getAllSales(0, 20);

        assertThat(result).isEmpty();
    }

    // ─── getSalesByBuyer ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getSalesByBuyer: retorna las ventas del comprador especificado")
    void getSalesByBuyer_returnsBuyerSales() {
        when(saleRepository.findByBuyerId("buyer-uuid")).thenReturn(List.of(sale));

        List<SaleDTO> result = ventasService.getSalesByBuyer("buyer-uuid");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBuyerEmail()).isEqualTo("carlos@example.com");
    }

    @Test
    @DisplayName("getSalesByBuyer: retorna lista vacía si el comprador no tiene ventas")
    void getSalesByBuyer_noBuyerSales_returnsEmptyList() {
        when(saleRepository.findByBuyerId("unknown-buyer")).thenReturn(List.of());

        List<SaleDTO> result = ventasService.getSalesByBuyer("unknown-buyer");

        assertThat(result).isEmpty();
    }
}
