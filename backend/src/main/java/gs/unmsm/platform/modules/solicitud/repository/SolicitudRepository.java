package gs.unmsm.platform.modules.solicitud.repository;

import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @EntityGraph(attributePaths = {"solicitante", "tecnico", "area", "categoria", "prioridad", "estado"})
    Optional<Solicitud> findByCodigo(String codigo);

    @EntityGraph(attributePaths = {"tecnico", "area", "categoria", "prioridad", "estado"})
    List<Solicitud> findBySolicitanteIdOrderByFechaCreacionDescIdDesc(Long solicitanteId);

    @EntityGraph(attributePaths = {"tecnico", "area", "categoria", "prioridad", "estado"})
    List<Solicitud> findByAreaIdOrderByFechaCreacionDescIdDesc(Long areaId);

    @EntityGraph(attributePaths = {"tecnico", "area", "categoria", "prioridad", "estado"})
    List<Solicitud> findByTecnicoIdOrderByFechaCreacionDescIdDesc(Long tecnicoId);

    @EntityGraph(attributePaths = {"tecnico", "area", "categoria", "prioridad", "estado"})
    List<Solicitud> findAllByOrderByFechaCreacionDescIdDesc();

    /** Solicitudes abiertas (ni Resuelto ni Cerrado) cuyo plazo vence antes de {@code limite}; usadas por las alertas de SLA. */
    @EntityGraph(attributePaths = {"area", "estado", "prioridad"})
    @Query("""
            select s from Solicitud s
            where s.estado.nombre not in ('Resuelto', 'Cerrado') and s.fechaVencimiento < :limite
            order by s.fechaVencimiento asc, s.id asc
            """)
    List<Solicitud> abiertasQueVencenAntesDe(@Param("limite") LocalDateTime limite);

    @Query("""
            select count(s) from Solicitud s
            where s.area.id = :areaId and s.estado.nombre not in ('Resuelto', 'Cerrado')
            """)
    long contarAbiertasDelArea(@Param("areaId") Long areaId);

    @Query("""
            select count(s) from Solicitud s
            where s.area.id = :areaId and s.estado.nombre not in ('Resuelto', 'Cerrado')
              and s.fechaVencimiento < :ahora
            """)
    long contarVencidasDelArea(@Param("areaId") Long areaId, @Param("ahora") LocalDateTime ahora);

    @Query("""
            select count(s) from Solicitud s
            where s.area.id = :areaId and s.estado.nombre not in ('Resuelto', 'Cerrado')
              and s.fechaVencimiento >= :desde and s.fechaVencimiento < :hasta
            """)
    long contarPorVencerDelArea(
            @Param("areaId") Long areaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
