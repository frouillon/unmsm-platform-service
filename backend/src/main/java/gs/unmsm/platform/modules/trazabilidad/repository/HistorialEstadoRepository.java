package gs.unmsm.platform.modules.trazabilidad.repository;

import gs.unmsm.platform.modules.trazabilidad.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findBySolicitudIdOrderByFechaRegistroAscIdAsc(Long solicitudId);
}
