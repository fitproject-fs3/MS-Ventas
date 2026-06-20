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

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleController {

    private final VentasService ventasService;

    @PostMapping
    public ResponseEntity<SaleDTO> createSale(@Valid @RequestBody SaleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventasService.createSale(request));
    }

    @GetMapping
    public ResponseEntity<List<SaleDTO>> getAllSales() {
        return ResponseEntity.ok(ventasService.getAllSales());
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<SaleDTO>> getSalesByBuyer(@PathVariable String buyerId) {
        return ResponseEntity.ok(ventasService.getSalesByBuyer(buyerId));
    }
}
