package gs.unmsm.platform.modules.solicitud.repository;

import gs.unmsm.platform.modules.solicitud.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {

    Optional<Prioridad> findByNombre(String nombre);
}
