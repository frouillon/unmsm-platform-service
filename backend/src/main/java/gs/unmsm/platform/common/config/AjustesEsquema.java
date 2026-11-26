package gs.unmsm.platform.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Cambios de esquema que {@code ddl-auto=update} no aplica sobre columnas existentes
 * (Hibernate no relaja restricciones NOT NULL). Idempotente.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class AjustesEsquema implements ApplicationRunner {

    private static final Map<String, String> UBICACION_OPCIONAL = Map.of(
            "pabellon", "varchar(100)",
            "nivel", "varchar(50)",
            "ambiente", "varchar(100)");

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        UBICACION_OPCIONAL.forEach((columna, tipo) -> admitirNulos("solicitud", columna, tipo));
        // El consolidado diario es un aviso general, sin solicitud asociada.
        admitirNulos("notificacion", "solicitud_id", "bigint");
    }

    /** Tabla, columna y tipo son constantes de esta clase; nunca vienen del usuario. */
    private void admitirNulos(String tabla, String columna, String tipo) {
        Integer obligatorias = jdbc.queryForObject("""
                select count(*) from information_schema.columns
                where table_schema = database() and table_name = ?
                  and column_name = ? and is_nullable = 'NO'
                """, Integer.class, tabla, columna);
        if (obligatorias != null && obligatorias > 0) {
            jdbc.execute("alter table " + tabla + " modify " + columna + " " + tipo + " null");
            log.info("Columna {}.{} ahora admite nulos.", tabla, columna);
        }
    }
}
