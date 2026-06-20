package com.fitproject.ventas.service;

import com.fitproject.ventas.dto.*;
import com.fitproject.ventas.model.*;
import com.fitproject.ventas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentasService {

    private final GymModelRepository modelRepository;
    private final GymUnitRepository unitRepository;
    private final SaleRepository saleRepository;

    public List<GymModelDTO> getAvailableModels() {
        return modelRepository.findByAvailableTrue().stream().map(this::toModelDTO).collect(Collectors.toList());
    }

    public List<GymModelDTO> getAllModels() {
        return modelRepository.findAll().stream().map(this::toModelDTO).collect(Collectors.toList());
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
        return toSaleDTO(saleRepository.save(sale));
    }

    public List<SaleDTO> getAllSales() {
        return saleRepository.findAll().stream().map(this::toSaleDTO).collect(Collectors.toList());
    }

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
