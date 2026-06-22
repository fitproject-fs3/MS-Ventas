package com.fitproject.ventas.repository;

import com.fitproject.ventas.model.GymModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link GymModel}.
 *
 * <p>El catálogo de modelos puede crecer con el tiempo; los métodos paginados
 * permiten al servidor cargar solo el subconjunto solicitado en lugar de todos
 * los registros (Green Computing).</p>
 */
public interface GymModelRepository extends JpaRepository<GymModel, String> {

    /**
     * Retorna los modelos disponibles para la venta con paginación.
     *
     * @param pageable configuración de página y tamaño
     * @return página de modelos con {@code available = true}
     */
    Page<GymModel> findByAvailableTrue(Pageable pageable);

    /** @deprecated Usar {@link #findByAvailableTrue(Pageable)} para evitar carga total. */
    @Deprecated
    List<GymModel> findByAvailableTrue();
}
