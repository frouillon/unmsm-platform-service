package gs.unmsm.platform.modules.usuario.repository;

import gs.unmsm.platform.modules.usuario.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoInstitucional(String correoInstitucional);

    @EntityGraph(attributePaths = {"rol", "area"})
    List<Usuario> findByRolNombreAndAreaIdAndActivoTrueOrderByNombresAsc(String rol, Long areaId);
}
