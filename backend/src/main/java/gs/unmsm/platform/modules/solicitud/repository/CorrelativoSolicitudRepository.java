package gs.unmsm.platform.modules.solicitud.repository;

import gs.unmsm.platform.modules.solicitud.model.CorrelativoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CorrelativoSolicitudRepository extends JpaRepository<CorrelativoSolicitud, Integer> {

    /**
     * Incrementa el correlativo del año en una sola sentencia atómica (MySQL) y deja el nuevo valor
     * en LAST_INSERT_ID() de la conexión; leerlo con {@link #ultimoGenerado()} antes de otro INSERT.
     */
    @Modifying
    @Query(nativeQuery = true, value = """
            insert into correlativo_solicitud (anio, ultimo) values (:anio, last_insert_id(1))
            on duplicate key update ultimo = last_insert_id(ultimo + 1)
            """)
    void incrementar(@Param("anio") int anio);

    @Query(nativeQuery = true, value = "select last_insert_id()")
    long ultimoGenerado();
}
