package com.fitproject.ventas.service;

import com.fitproject.ventas.config.RabbitMQConfig;
import com.fitproject.ventas.dto.*;
import com.fitproject.ventas.messaging.NotificationEvent;
import com.fitproject.ventas.model.*;
import com.fitproject.ventas.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentasService {

    private final GymModelRepository modelRepository;
    private final GymUnitRepository unitRepository;
    private final SaleRepository saleRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Retorna los modelos disponibles para la venta con paginación para limitar
     * la carga en memoria cuando el catálogo crezca (Green Computing).
     *
     * @param page número de página, base cero
     * @param size cantidad máxima de modelos por página
     * @return lista paginada de modelos con {@code available = true}
     */
    public List<GymModelDTO> getAvailableModels(int page, int size) {
        return modelRepository.findByAvailableTrue(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::toModelDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna el catálogo completo de modelos con paginación (uso administrativo).
     *
     * @param page número de página, base cero
     * @param size cantidad máxima de modelos por página
     * @return lista paginada del catálogo completo
     */
    public List<GymModelDTO> getAllModels(int page, int size) {
        return modelRepository.findAll(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::toModelDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SaleDTO createSale(SaleRequestDTO req) {
        GymUnit unit = unitRepository.findById(req.getUnitId())
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada: " + req.getUnitId()));
        if (unit.getStatus() != UnitStatus.AVAILABLE)
            throw new IllegalStateException("La unidad no está disponible para la venta");
        unit.setStatus(UnitStatus.SOLD);
        unitRepository.save(unit);
        Sale sale = Sale.builder()
                .unit(unit)
                .buyerId(req.getBuyerId())
                .buyerName(req.getBuyerName())
                .buyerEmail(req.getBuyerEmail())
                .salePrice(unit.getModel().getPrice())
                .status(SaleStatus.CONFIRMED)
                .build();
        SaleDTO result = toSaleDTO(saleRepository.save(sale));
        publishSaleConfirmedEvent(sale, unit.getModel().getName());
        return result;
    }

    /**
     * Publica un evento {@code sale.confirmed} en el Exchange de RabbitMQ.
     * MS-Notificaciones consume este evento y envía la confirmación de compra al cliente.
     *
     * @param sale      venta persistida con los datos del comprador
     * @param modelName nombre del modelo de gimnasio adquirido
     */
    private void publishSaleConfirmedEvent(Sale sale, String modelName) {
        NotificationEvent event = new NotificationEvent(
                sale.getBuyerEmail(),
                sale.getBuyerName(),
                "Confirmación de compra — FitProject",
                "¡Hola " + sale.getBuyerName() + "! Tu compra del módulo '"
                        + modelName + "' ha sido confirmada con éxito. "
                        + "Recibirás más información sobre el proceso de fabricación en breve.",
                "SALE_CONFIRMED"
        );
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATIONS_EXCHANGE,
                    RabbitMQConfig.SALE_CONFIRMED_KEY,
                    event
            );
            log.info("[RabbitMQ] Evento SALE_CONFIRMED publicado para venta {}", sale.getSaleId());
        } catch (Exception ex) {
            log.error("[RabbitMQ] Error al publicar SALE_CONFIRMED para {}: {}", sale.getSaleId(), ex.getMessage());
        }
    }

    /**
     * Retorna todas las ventas con paginación para evitar cargar el historial
     * completo en memoria cuando el volumen de transacciones escale (Green Computing).
     *
     * @param page número de página, base cero
     * @param size cantidad máxima de ventas por página
     * @return lista paginada de ventas
     */
    public List<SaleDTO> getAllSales(int page, int size) {
        return saleRepository.findAll(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::toSaleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna las ventas de un comprador específico.
     *
     * @param buyerId identificador UUID del comprador
     * @return lista de ventas del comprador
     */
    public List<SaleDTO> getSalesByBuyer(String buyerId) {
        return saleRepository.findByBuyerId(buyerId).stream().map(this::toSaleDTO).collect(Collectors.toList());
    }

    private GymModelDTO toModelDTO(GymModel m) {
        return GymModelDTO.builder()
                .modelId(m.getModelId()).name(m.getName()).description(m.getDescription())
                .price(m.getPrice()).areaM2(m.getAreaM2()).capacity(m.getCapacity())
                .imageUrl(m.getImageUrl()).available(m.getAvailable())
                .build();
    }

    private SaleDTO toSaleDTO(Sale s) {
        return SaleDTO.builder()
                .saleId(s.getSaleId())
                .unitId(s.getUnit().getUnitId())
                .modelName(s.getUnit().getModel().getName())
                .buyerName(s.getBuyerName()).buyerEmail(s.getBuyerEmail())
                .salePrice(s.getSalePrice()).status(s.getStatus().name())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
