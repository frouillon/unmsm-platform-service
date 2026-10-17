package gs.unmsm.platform.modules.solicitud.repository;

import gs.unmsm.platform.modules.solicitud.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {

    Optional<Prioridad> findByNombre(String nombre);

    /** De menor a mayor urgencia: Baja, Media, Alta, Urgente. */
    List<Prioridad> findAllByOrderBySlaDiasDesc();
}
