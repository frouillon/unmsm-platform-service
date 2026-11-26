package gs.unmsm.platform.modules.notificacion.repository;

import gs.unmsm.platform.modules.notificacion.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDescIdDesc(Long usuarioId);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    Optional<Notificacion> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Modifying(clearAutomatically = true)
    @Query("update Notificacion n set n.leida = true where n.usuario.id = :usuarioId and n.leida = false")
    int marcarTodasLeidas(@Param("usuarioId") Long usuarioId);

    /** Evita repetir la misma alerta de SLA al mismo usuario por la misma solicitud. */
    boolean existsByUsuarioIdAndSolicitudIdAndTipo(Long usuarioId, Long solicitudId, String tipo);

    /** Evita enviar dos consolidados el mismo día (por ejemplo, si el servidor se reinicia). */
    boolean existsByUsuarioIdAndTipoAndFechaCreacionGreaterThanEqual(Long usuarioId, String tipo, LocalDateTime desde);
}
