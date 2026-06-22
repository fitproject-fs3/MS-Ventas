package com.fitproject.ventas.controller;

import com.fitproject.ventas.dto.SaleDTO;
import com.fitproject.ventas.dto.SaleRequestDTO;
import com.fitproject.ventas.service.VentasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para la gestión de ventas de módulos de gimnasio.
 *
 * <p>Una venta ({@link com.fitproject.ventas.model.Sale}) representa la adquisición
 * de una unidad ({@link com.fitproject.ventas.model.GymUnit}) por parte de un comprador.
 * Al confirmar la venta, la unidad pasa a estado {@code SOLD} y se publica un evento
 * {@code sale.confirmed} en RabbitMQ para que MS-Notificaciones envíe el email de
 * confirmación al cliente.</p>
 *
 * <p>Base URL: {@code /api/v1/sales}</p>
 *
 * @see VentasService
 */
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleController {

    private final VentasService ventasService;

    /**
     * Crea una nueva venta, marca la unidad como vendida y notifica al comprador por email.
     *
     * @param request datos de la venta: {@code unitId}, {@code buyerId},
     *                {@code buyerName}, {@code buyerEmail}
     * @return venta creada con status 201 Created
     * @throws IllegalStateException    si la unidad no está disponible para la venta
     * @throws IllegalArgumentException si la unidad referenciada no existe
     */
    @PostMapping
    public ResponseEntity<SaleDTO> createSale(@Valid @RequestBody SaleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventasService.createSale(request));
    }

    /**
     * Lista ventas registradas en el sistema con paginación para evitar cargar
     * el historial completo de transacciones en memoria (Green Computing).
     *
     * @param page número de página, base cero (default: 0)
     * @param size cantidad máxima de ventas por página (default: 20)
     * @return lista paginada de ventas
     */
    @GetMapping
    public ResponseEntity<List<SaleDTO>> getAllSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ventasService.getAllSales(page, size));
    }

    /**
     * Obtiene todas las ventas realizadas por un comprador específico.
     *
     * @param buyerId identificador UUID del comprador
     * @return lista de ventas del comprador
     */
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<SaleDTO>> getSalesByBuyer(@PathVariable String buyerId) {
        return ResponseEntity.ok(ventasService.getSalesByBuyer(buyerId));
    }
}
