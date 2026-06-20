package com.fitproject.ventas.repository;

import com.fitproject.ventas.model.Sale;
import com.fitproject.ventas.model.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, String> {
    List<Sale> findByBuyerId(String buyerId);
    List<Sale> findByStatus(SaleStatus status);
    long countByStatus(SaleStatus status);
}
