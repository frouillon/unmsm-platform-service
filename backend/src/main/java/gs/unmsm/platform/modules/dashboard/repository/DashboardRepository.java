package gs.unmsm.platform.modules.dashboard.repository;

import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository extends JpaRepository<Solicitud, Long> {

    @Query("""
            select count(s) from Solicitud s
            where (:solicitanteId is null or s.solicitante.id = :solicitanteId)
              and (:tecnicoId is null or s.tecnico.id = :tecnicoId)
              and (:areaId is null or s.area.id = :areaId)
            """)
    long total(
            @Param("solicitanteId") Long solicitanteId,
            @Param("tecnicoId") Long tecnicoId,
            @Param("areaId") Long areaId);

    @Query("""
            select count(s) from Solicitud s
            where (:solicitanteId is null or s.solicitante.id = :solicitanteId)
              and (:tecnicoId is null or s.tecnico.id = :tecnicoId)
              and (:areaId is null or s.area.id = :areaId)
              and s.fechaVencimiento < :ahora
              and s.estado.nombre not in ('Resuelto', 'Cerrado')
            """)
    long vencidas(
            @Param("solicitanteId") Long solicitanteId,
            @Param("tecnicoId") Long tecnicoId,
            @Param("areaId") Long areaId,
            @Param("ahora") LocalDateTime ahora);

    @Query("""
            select s.estado.nombre, count(s) from Solicitud s
            where (:solicitanteId is null or s.solicitante.id = :solicitanteId)
              and (:tecnicoId is null or s.tecnico.id = :tecnicoId)
              and (:areaId is null or s.area.id = :areaId)
            group by s.estado.nombre
            order by s.estado.nombre
            """)
    List<Object[]> porEstado(
            @Param("solicitanteId") Long solicitanteId,
            @Param("tecnicoId") Long tecnicoId,
            @Param("areaId") Long areaId);

    @Query("""
            select s.area.nombre, count(s) from Solicitud s
            where (:solicitanteId is null or s.solicitante.id = :solicitanteId)
              and (:tecnicoId is null or s.tecnico.id = :tecnicoId)
              and (:areaId is null or s.area.id = :areaId)
            group by s.area.nombre
            order by s.area.nombre
            """)
    List<Object[]> porArea(
            @Param("solicitanteId") Long solicitanteId,
            @Param("tecnicoId") Long tecnicoId,
            @Param("areaId") Long areaId);

    @Query("""
            select s.prioridad.nombre, count(s) from Solicitud s
            where (:solicitanteId is null or s.solicitante.id = :solicitanteId)
              and (:tecnicoId is null or s.tecnico.id = :tecnicoId)
              and (:areaId is null or s.area.id = :areaId)
            group by s.prioridad.nombre
            order by s.prioridad.nombre
            """)
    List<Object[]> porPrioridad(
            @Param("solicitanteId") Long solicitanteId,
            @Param("tecnicoId") Long tecnicoId,
            @Param("areaId") Long areaId);
}
