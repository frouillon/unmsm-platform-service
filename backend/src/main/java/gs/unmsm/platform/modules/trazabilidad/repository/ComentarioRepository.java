package gs.unmsm.platform.modules.trazabilidad.repository;

import gs.unmsm.platform.modules.trazabilidad.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findBySolicitudIdOrderByFechaCreacionAscIdAsc(Long solicitudId);
}
