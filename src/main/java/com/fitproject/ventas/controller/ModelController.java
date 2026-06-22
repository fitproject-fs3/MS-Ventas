package com.fitproject.ventas.controller;

import com.fitproject.ventas.dto.GymModelDTO;
import com.fitproject.ventas.service.VentasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para el catálogo de modelos de gimnasios en contenedor.
 *
 * <p>Un {@link com.fitproject.ventas.model.GymModel} define las características
 * del producto (nombre, precio, área, capacidad). Cada modelo puede tener múltiples
 * unidades ({@link com.fitproject.ventas.model.GymUnit}) en distintos estados
 * ({@code AVAILABLE}, {@code RESERVED}, {@code SOLD}).</p>
 *
 * <p>Base URL: {@code /api/v1/models}</p>
 *
 * @see VentasService
 */
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModelController {

    private final VentasService ventasService;

    /**
     * Lista los modelos de gimnasio disponibles para la venta con paginación.
     *
     * <p>Solo retorna modelos con {@code available = true}. La paginación evita
     * cargar el catálogo completo en memoria cuando el número de modelos crezca
     * (Green Computing).</p>
     *
     * @param page número de página, base cero (default: 0)
     * @param size cantidad máxima de modelos por página (default: 20)
     * @return lista paginada de modelos disponibles para compra
     */
    @GetMapping
    public ResponseEntity<List<GymModelDTO>> getAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ventasService.getAvailableModels(page, size));
    }

    /**
     * Lista todos los modelos del catálogo con paginación (uso administrativo).
     *
     * <p>Incluye modelos con {@code available = false}. La paginación limita el
     * consumo de memoria del servidor (Green Computing).</p>
     *
     * @param page número de página, base cero (default: 0)
     * @param size cantidad máxima de modelos por página (default: 20)
     * @return lista paginada del catálogo completo
     */
    @GetMapping("/all")
    public ResponseEntity<List<GymModelDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ventasService.getAllModels(page, size));
    }
}
