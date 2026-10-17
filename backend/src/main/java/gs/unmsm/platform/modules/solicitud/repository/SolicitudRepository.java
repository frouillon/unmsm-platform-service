package gs.unmsm.platform.modules.solicitud.repository;

import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @EntityGraph(attributePaths = {"solicitante", "tecnico", "area", "categoria", "prioridad", "estado"})
    Optional<Solicitud> findByCodigo(String codigo);

    @EntityGraph(attributePaths = {"tecnico", "area", "categoria", "prioridad", "estado"})
    List<Solicitud> findBySolicitanteIdOrderByFechaCreacionDescIdDesc(Long solicitanteId);
}
