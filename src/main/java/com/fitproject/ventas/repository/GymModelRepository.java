package com.fitproject.ventas.repository;

import com.fitproject.ventas.model.GymModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GymModelRepository extends JpaRepository<GymModel, String> {
    List<GymModel> findByAvailableTrue();
}
