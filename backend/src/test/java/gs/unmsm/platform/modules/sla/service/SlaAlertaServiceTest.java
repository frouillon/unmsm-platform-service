package gs.unmsm.platform.modules.sla.service;

import gs.unmsm.platform.modules.notificacion.model.Notificacion;
import gs.unmsm.platform.modules.notificacion.repository.NotificacionRepository;
import gs.unmsm.platform.modules.notificacion.service.NotificacionService;
import gs.unmsm.platform.modules.sla.dto.ResultadoConsolidado;
import gs.unmsm.platform.modules.sla.dto.ResultadoEvaluacionSla;
import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.usuario.model.Area;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaAlertaServiceTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 26, 10, 0);

    @Mock SolicitudRepository solicitudes;
    @Mock UsuarioRepository usuarios;
    @Mock NotificacionRepository notificaciones;

    private final MeterRegistry metricas = new SimpleMeterRegistry();
    private SlaAlertaService servicio;

    private final Area mantenimiento = Area.builder().id(1L).nombre("Mantenimiento general").build();
    private final Usuario ronald = Usuario.builder().id(10L).nombres("Ronald Munayco").area(mantenimiento).build();
    private final Usuario marcos = Usuario.builder().id(20L).nombres("Marcos Vidal").build();

    @BeforeEach
    void crearServicio() {
        Clock reloj = Clock.fixed(AHORA.atZone(LIMA).toInstant(), LIMA);
        servicio = new SlaAlertaService(solicitudes, usuarios, notificaciones, reloj, metricas, 24);
    }

    @Test
    void avisaAlSupervisorDelAreaSiLaSolicitudVencioOEstaPorVencer() {
        Solicitud vencida = solicitud(1L, "SOL-2026-0001", AHORA.minusHours(3), EstadoNombre.ASIGNADO);
        Solicitud enRiesgo = solicitud(2L, "SOL-2026-0002", AHORA.plusHours(5), EstadoNombre.PENDIENTE);
        when(solicitudes.abiertasQueVencenAntesDe(AHORA.plusHours(24))).thenReturn(List.of(vencida, enRiesgo));
        when(usuarios.findByRolNombreAndAreaIdAndActivoTrueOrderByNombresAsc(RolNombre.SUPERVISOR, 1L))
                .thenReturn(List.of(ronald));

        ResultadoEvaluacionSla r = servicio.evaluar();

        assertThat(r).isEqualTo(new ResultadoEvaluacionSla(2, 1, 1, 2));
        ArgumentCaptor<Notificacion> guardadas = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificaciones, times(2)).save(guardadas.capture());
        assertThat(guardadas.getAllValues()).extracting(Notificacion::getTipo)
                .containsExactly(NotificacionService.TIPO_SLA_VENCIDA, NotificacionService.TIPO_SLA_EN_RIESGO);
        assertThat(guardadas.getAllValues()).allSatisfy(n -> assertThat(n.getUsuario()).isSameAs(ronald));
        assertThat(guardadas.getAllValues().get(0).getMensaje())
                .isEqualTo("La solicitud SOL-2026-0001 venció el 26/11/2026 a las 07:00 sin resolverse (estado: Asignado).");
        assertThat(guardadas.getAllValues().get(1).getMensaje())
                .isEqualTo("La solicitud SOL-2026-0002 vence el 26/11/2026 a las 15:00 y sigue en estado Pendiente.");
        // Los supervisores del área se buscan una sola vez por pasada.
        verify(usuarios, times(1)).findByRolNombreAndAreaIdAndActivoTrueOrderByNombresAsc(RolNombre.SUPERVISOR, 1L);
        assertThat(metricas.get("sanmarcos.sla.alertas").tag("tipo", "vencida").counter().count()).isEqualTo(1.0);
        assertThat(metricas.get("sanmarcos.solicitudes.vencidas").gauge().value()).isEqualTo(1.0);
    }

    @Test
    void noRepiteUnaAlertaYaEnviada() {
        Solicitud vencida = solicitud(1L, "SOL-2026-0001", AHORA.minusDays(1), EstadoNombre.EN_PROCESO);
        when(solicitudes.abiertasQueVencenAntesDe(any())).thenReturn(List.of(vencida));
        when(usuarios.findByRolNombreAndAreaIdAndActivoTrueOrderByNombresAsc(RolNombre.SUPERVISOR, 1L))
                .thenReturn(List.of(ronald));
        when(notificaciones.existsByUsuarioIdAndSolicitudIdAndTipo(10L, 1L, NotificacionService.TIPO_SLA_VENCIDA))
                .thenReturn(true);

        ResultadoEvaluacionSla r = servicio.evaluar();

        assertThat(r.vencidas()).isEqualTo(1);
        assertThat(r.alertasEmitidas()).isZero();
        verify(notificaciones, never()).save(any());
    }

    @Test
    void siElAreaNoTieneSupervisorAvisaALosAdministradores() {
        Solicitud vencida = solicitud(1L, "SOL-2026-0001", AHORA.minusHours(1), EstadoNombre.PENDIENTE);
        when(solicitudes.abiertasQueVencenAntesDe(any())).thenReturn(List.of(vencida));
        when(usuarios.findByRolNombreAndAreaIdAndActivoTrueOrderByNombresAsc(RolNombre.SUPERVISOR, 1L))
                .thenReturn(List.of());
        when(usuarios.findByRolNombreAndActivoTrueOrderByNombresAsc(RolNombre.ADMINISTRADOR))
                .thenReturn(List.of(marcos));

        servicio.evaluar();

        ArgumentCaptor<Notificacion> guardada = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificaciones).save(guardada.capture());
        assertThat(guardada.getValue().getUsuario()).isSameAs(marcos);
    }

    @Test
    void sinSolicitudesCriticasNoNotificaNada() {
        when(solicitudes.abiertasQueVencenAntesDe(any())).thenReturn(List.of());

        assertThat(servicio.evaluar()).isEqualTo(new ResultadoEvaluacionSla(0, 0, 0, 0));
        verify(notificaciones, never()).save(any());
    }

    @Test
    void elConsolidadoResumeElAreaDeCadaSupervisor() {
        when(usuarios.findByRolNombreAndActivoTrueOrderByNombresAsc(RolNombre.SUPERVISOR)).thenReturn(List.of(ronald));
        when(solicitudes.contarAbiertasDelArea(1L)).thenReturn(5L);
        when(solicitudes.contarVencidasDelArea(1L, AHORA)).thenReturn(1L);
        when(solicitudes.contarPorVencerDelArea(1L, AHORA, AHORA.plusHours(24))).thenReturn(2L);

        ResultadoConsolidado r = servicio.enviarConsolidado();

        assertThat(r.enviados()).isEqualTo(1);
        ArgumentCaptor<Notificacion> guardada = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificaciones).save(guardada.capture());
        assertThat(guardada.getValue().getTipo()).isEqualTo(NotificacionService.TIPO_CONSOLIDADO_DIARIO);
        assertThat(guardada.getValue().getSolicitud()).isNull();
        assertThat(guardada.getValue().getMensaje()).isEqualTo(
                "Resumen diario de Mantenimiento general: 5 solicitudes abiertas, 1 vencida y 2 por vencer en las próximas 24 h.");
    }

    @Test
    void elConsolidadoNoSeEnviaDosVecesElMismoDia() {
        Usuario sinArea = Usuario.builder().id(11L).nombres("Supervisor sin área").build();
        when(usuarios.findByRolNombreAndActivoTrueOrderByNombresAsc(RolNombre.SUPERVISOR))
                .thenReturn(List.of(ronald, sinArea));
        when(notificaciones.existsByUsuarioIdAndTipoAndFechaCreacionGreaterThanEqual(
                eq(10L), eq(NotificacionService.TIPO_CONSOLIDADO_DIARIO), eq(AHORA.toLocalDate().atStartOfDay())))
                .thenReturn(true);

        assertThat(servicio.enviarConsolidado().enviados()).isZero();
        verify(notificaciones, never()).save(any());
        verify(solicitudes, never()).contarAbiertasDelArea(anyLong());
    }

    private Solicitud solicitud(Long id, String codigo, LocalDateTime vencimiento, String estado) {
        return Solicitud.builder()
                .id(id)
                .codigo(codigo)
                .area(mantenimiento)
                .estado(Estado.builder().nombre(estado).build())
                .fechaVencimiento(vencimiento)
                .build();
    }
}
