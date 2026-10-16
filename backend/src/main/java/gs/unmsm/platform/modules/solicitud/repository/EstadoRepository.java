package gs.unmsm.platform.modules.solicitud.repository;

import gs.unmsm.platform.modules.solicitud.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Long> {

    Optional<Estado> findByNombre(String nombre);
}
