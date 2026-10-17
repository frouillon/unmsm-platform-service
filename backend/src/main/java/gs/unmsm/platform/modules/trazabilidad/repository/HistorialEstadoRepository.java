package gs.unmsm.platform.modules.trazabilidad.repository;

import gs.unmsm.platform.modules.trazabilidad.model.HistorialEstado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    @EntityGraph(attributePaths = {"estadoOrigen", "estadoDestino", "usuario"})
    List<HistorialEstado> findBySolicitudIdOrderByFechaRegistroAscIdAsc(Long solicitudId);

    interface UltimoMovimiento {
        Long getSolicitudId();

        LocalDateTime getFecha();
    }

    @Query("""
            select h.solicitud.id as solicitudId, max(h.fechaRegistro) as fecha
            from HistorialEstado h where h.solicitud.id in :ids group by h.solicitud.id
            """)
    List<UltimoMovimiento> ultimosMovimientos(@Param("ids") Collection<Long> ids);
}
