package gs.unmsm.platform.modules.sla.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Procesos programados de SLA (sección 9.9.4 del documento de arquitectura). Corren fuera de las peticiones
 * de los usuarios; un fallo se registra en el log y no detiene las siguientes ejecuciones.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sla.alertas.habilitado", havingValue = "true", matchIfMissing = true)
public class SlaAlertaJob {

    /** Misma clave de MDC que usan las peticiones HTTP para su id de correlación. */
    private static final String MDC_CORRELACION = "correlationId";

    private final SlaAlertaService servicio;

    @Scheduled(cron = "${app.sla.alertas.cron:0 */15 * * * *}", zone = "${app.zona-horaria:America/Lima}")
    public void evaluarPlazos() {
        ejecutar("sla-alertas", servicio::evaluar);
    }

    @Scheduled(cron = "${app.sla.consolidado.cron:0 0 8 * * MON-FRI}", zone = "${app.zona-horaria:America/Lima}")
    public void enviarConsolidado() {
        ejecutar("sla-consolidado", servicio::enviarConsolidado);
    }

    /** Cada ejecución lleva su propio id de correlación, igual que una petición HTTP. */
    private void ejecutar(String nombre, Runnable tarea) {
        MDC.put(MDC_CORRELACION, nombre + "-" + UUID.randomUUID().toString().substring(0, 8));
        try {
            tarea.run();
        } catch (RuntimeException e) {
            log.error("Falló el proceso programado {}.", nombre, e);
        } finally {
            MDC.remove(MDC_CORRELACION);
        }
    }
}
