package com.fitproject.ventas.repository;

import com.fitproject.ventas.model.GymUnit;
import com.fitproject.ventas.model.UnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GymUnitRepository extends JpaRepository<GymUnit, String> {
    List<GymUnit> findByStatus(UnitStatus status);
    List<GymUnit> findByModelModelId(String modelId);
}
