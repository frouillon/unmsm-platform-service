package gs.unmsm.platform.modules.sla.service;

import gs.unmsm.platform.modules.notificacion.model.Notificacion;
import gs.unmsm.platform.modules.notificacion.repository.NotificacionRepository;
import gs.unmsm.platform.modules.notificacion.service.NotificacionService;
import gs.unmsm.platform.modules.sla.dto.ResultadoConsolidado;
import gs.unmsm.platform.modules.sla.dto.ResultadoEvaluacionSla;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Alertas de SLA (HU-21, RF-37/RF-38).
 *
 * <ul>
 *   <li><b>Evaluación periódica:</b> busca solicitudes abiertas que vencen dentro del umbral (en riesgo) o que
 *   ya vencieron, y avisa a los supervisores del área. Si el área no tiene supervisor activo, avisa a los
 *   administradores. Cada aviso se emite una sola vez por usuario, solicitud y tipo.</li>
 *   <li><b>Consolidado diario:</b> resumen matutino del área para cada supervisor, como máximo uno por día.</li>
 * </ul>
 *
 * También publica métricas de negocio (solicitudes en riesgo, vencidas y alertas emitidas) para Prometheus.
 */
@Slf4j
@Service
public class SlaAlertaService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private final SolicitudRepository solicitudes;
    private final UsuarioRepository usuarios;
    private final NotificacionRepository notificaciones;
    private final Clock clock;
    private final int umbralHoras;

    private final AtomicLong enRiesgoActual = new AtomicLong();
    private final AtomicLong vencidasActual = new AtomicLong();
    private final Counter alertasRiesgo;
    private final Counter alertasVencida;
    private final Counter consolidados;
    private final Timer duracionEvaluacion;

    public SlaAlertaService(
            SolicitudRepository solicitudes,
            UsuarioRepository usuarios,
            NotificacionRepository notificaciones,
            Clock clock,
            MeterRegistry metricas,
            @Value("${app.sla.alertas.umbral-horas:24}") int umbralHoras) {
        this.solicitudes = solicitudes;
        this.usuarios = usuarios;
        this.notificaciones = notificaciones;
        this.clock = clock;
        this.umbralHoras = Math.max(1, umbralHoras);

        metricas.gauge("sanmarcos.solicitudes.en_riesgo", enRiesgoActual);
        metricas.gauge("sanmarcos.solicitudes.vencidas", vencidasActual);
        this.alertasRiesgo = Counter.builder("sanmarcos.sla.alertas")
                .description("Alertas de SLA emitidas").tag("tipo", "en_riesgo").register(metricas);
        this.alertasVencida = Counter.builder("sanmarcos.sla.alertas")
                .description("Alertas de SLA emitidas").tag("tipo", "vencida").register(metricas);
        this.consolidados = Counter.builder("sanmarcos.sla.consolidados")
                .description("Consolidados diarios enviados a supervisores").register(metricas);
        this.duracionEvaluacion = Timer.builder("sanmarcos.sla.evaluacion")
                .description("Duración de cada pasada del proceso de alertas de SLA").register(metricas);
    }

    @Transactional
    public ResultadoEvaluacionSla evaluar() {
        Timer.Sample cronometro = Timer.start();
        LocalDateTime ahora = ahora();
        List<Solicitud> criticas = solicitudes.abiertasQueVencenAntesDe(ahora.plusHours(umbralHoras));
        Map<Long, List<Usuario>> destinatariosPorArea = new HashMap<>();

        int enRiesgo = 0;
        int vencidas = 0;
        int emitidas = 0;
        for (Solicitud s : criticas) {
            boolean vencida = s.getFechaVencimiento().isBefore(ahora);
            if (vencida) {
                vencidas++;
            } else {
                enRiesgo++;
            }
            String tipo = vencida ? NotificacionService.TIPO_SLA_VENCIDA : NotificacionService.TIPO_SLA_EN_RIESGO;
            String mensaje = vencida ? mensajeVencida(s) : mensajeEnRiesgo(s);

            List<Usuario> destinatarios = destinatariosPorArea.computeIfAbsent(s.getArea().getId(), this::destinatarios);
            for (Usuario destinatario : destinatarios) {
                if (notificaciones.existsByUsuarioIdAndSolicitudIdAndTipo(destinatario.getId(), s.getId(), tipo)) {
                    continue;
                }
                notificaciones.save(Notificacion.builder()
                        .usuario(destinatario)
                        .solicitud(s)
                        .tipo(tipo)
                        .mensaje(recortar(mensaje))
                        .leida(false)
                        .fechaCreacion(ahora)
                        .build());
                emitidas++;
                (vencida ? alertasVencida : alertasRiesgo).increment();
            }
        }

        enRiesgoActual.set(enRiesgo);
        vencidasActual.set(vencidas);
        cronometro.stop(duracionEvaluacion);
        if (emitidas > 0) {
            log.info("Alertas de SLA: {} en riesgo, {} vencidas, {} avisos nuevos.", enRiesgo, vencidas, emitidas);
        } else {
            log.debug("Alertas de SLA: {} en riesgo, {} vencidas, sin avisos nuevos.", enRiesgo, vencidas);
        }
        return new ResultadoEvaluacionSla(criticas.size(), enRiesgo, vencidas, emitidas);
    }

    @Transactional
    public ResultadoConsolidado enviarConsolidado() {
        LocalDateTime ahora = ahora();
        LocalDateTime inicioDelDia = ahora.toLocalDate().atStartOfDay();
        int enviados = 0;

        for (Usuario supervisor : usuarios.findByRolNombreAndActivoTrueOrderByNombresAsc(RolNombre.SUPERVISOR)) {
            if (supervisor.getArea() == null) {
                continue;
            }
            if (notificaciones.existsByUsuarioIdAndTipoAndFechaCreacionGreaterThanEqual(
                    supervisor.getId(), NotificacionService.TIPO_CONSOLIDADO_DIARIO, inicioDelDia)) {
                continue;
            }
            Long areaId = supervisor.getArea().getId();
            long abiertas = solicitudes.contarAbiertasDelArea(areaId);
            long vencidas = solicitudes.contarVencidasDelArea(areaId, ahora);
            long porVencer = solicitudes.contarPorVencerDelArea(areaId, ahora, ahora.plusHours(umbralHoras));

            notificaciones.save(Notificacion.builder()
                    .usuario(supervisor)
                    .tipo(NotificacionService.TIPO_CONSOLIDADO_DIARIO)
                    .mensaje(recortar(mensajeConsolidado(supervisor.getArea().getNombre(), abiertas, vencidas, porVencer)))
                    .leida(false)
                    .fechaCreacion(ahora)
                    .build());
            enviados++;
            consolidados.increment();
        }
        log.info("Consolidado diario enviado a {} supervisores.", enviados);
        return new ResultadoConsolidado(enviados);
    }

    /** Supervisores activos del área; si no hay, los administradores, para que la alerta no se pierda. */
    private List<Usuario> destinatarios(Long areaId) {
        List<Usuario> supervisores =
                usuarios.findByRolNombreAndAreaIdAndActivoTrueOrderByNombresAsc(RolNombre.SUPERVISOR, areaId);
        if (!supervisores.isEmpty()) {
            return supervisores;
        }
        return usuarios.findByRolNombreAndActivoTrueOrderByNombresAsc(RolNombre.ADMINISTRADOR);
    }

    static String mensajeEnRiesgo(Solicitud s) {
        return "La solicitud %s vence el %s y sigue en estado %s."
                .formatted(s.getCodigo(), FECHA.format(s.getFechaVencimiento()), s.getEstado().getNombre());
    }

    static String mensajeVencida(Solicitud s) {
        return "La solicitud %s venció el %s sin resolverse (estado: %s)."
                .formatted(s.getCodigo(), FECHA.format(s.getFechaVencimiento()), s.getEstado().getNombre());
    }

    String mensajeConsolidado(String area, long abiertas, long vencidas, long porVencer) {
        return "Resumen diario de %s: %s, %s y %s en las próximas %d h.".formatted(
                area,
                plural(abiertas, "solicitud abierta", "solicitudes abiertas"),
                plural(vencidas, "vencida", "vencidas"),
                plural(porVencer, "por vencer", "por vencer"),
                umbralHoras);
    }

    private static String plural(long n, String singular, String plural) {
        return n + " " + (n == 1 ? singular : plural);
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

    private static String recortar(String mensaje) {
        return mensaje.length() <= 255 ? mensaje : mensaje.substring(0, 252) + "...";
    }
}
