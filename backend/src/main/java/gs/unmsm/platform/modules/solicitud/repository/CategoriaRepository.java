package gs.unmsm.platform.modules.solicitud.repository;

import gs.unmsm.platform.modules.solicitud.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);
}
