package com.fitproject.ventas.controller;

import com.fitproject.ventas.dto.GymModelDTO;
import com.fitproject.ventas.service.VentasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModelController {

    private final VentasService ventasService;

    @GetMapping
    public ResponseEntity<List<GymModelDTO>> getAvailable() {
        return ResponseEntity.ok(ventasService.getAvailableModels());
    }

    @GetMapping("/all")
    public ResponseEntity<List<GymModelDTO>> getAll() {
        return ResponseEntity.ok(ventasService.getAllModels());
    }
}
