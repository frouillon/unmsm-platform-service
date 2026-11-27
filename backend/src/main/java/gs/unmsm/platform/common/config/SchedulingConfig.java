package gs.unmsm.platform.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Activa el planificador de tareas (alertas de SLA y consolidado diario).
 * Se apaga con {@code app.sla.alertas.habilitado=false}; las pruebas lo hacen para no depender del reloj.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.sla.alertas.habilitado", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
