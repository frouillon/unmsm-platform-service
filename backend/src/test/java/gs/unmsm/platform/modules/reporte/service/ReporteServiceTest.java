package gs.unmsm.platform.modules.reporte.service;

import gs.unmsm.platform.common.exception.FieldValidationException;
import gs.unmsm.platform.common.exception.ForbiddenException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.reporte.dto.FilaReporte;
import gs.unmsm.platform.modules.reporte.dto.ReporteSolicitudesResponse;
import gs.unmsm.platform.modules.solicitud.model.Categoria;
import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Prioridad;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository.UltimoMovimiento;
import gs.unmsm.platform.modules.usuario.model.Area;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.AreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 26, 12, 0);

    @Mock SolicitudRepository solicitudes;
    @Mock HistorialEstadoRepository historial;
    @Mock AreaRepository areas;

    private ReporteService servicio;

    private final Area mantenimiento = Area.builder().id(1L).nombre("Mantenimiento general").activo(true).build();
    private final UsuarioAutenticado supervisor =
            new UsuarioAutenticado(10L, "Ronald Munayco", RolNombre.SUPERVISOR, 1L);
    private final UsuarioAutenticado admin =
            new UsuarioAutenticado(20L, "Marcos Vidal", RolNombre.ADMINISTRADOR, 3L);

    @BeforeEach
    void crearServicio() {
        Clock reloj = Clock.fixed(AHORA.atZone(LIMA).toInstant(), LIMA);
        servicio = new ReporteService(solicitudes, historial, areas, reloj);
    }

    @Test
    void calculaCumplimientoMttrYCargaTecnica() {
        Usuario diana = Usuario.builder().id(5L).nombres("Diana Cárdenas").build();
        // Resuelta en 1.5 días, antes de su fecha límite.
        Solicitud aTiempo = solicitud(1L, EstadoNombre.RESUELTO, AHORA.minusDays(5), AHORA.minusDays(2), diana);
        // Cerrada 6 días después de registrarse, con 3 días de plazo.
        Solicitud tarde = solicitud(2L, EstadoNombre.CERRADO, AHORA.minusDays(10), AHORA.minusDays(7), diana);
        // Abierta y ya vencida.
        Solicitud vencida = solicitud(3L, EstadoNombre.EN_PROCESO, AHORA.minusDays(4), AHORA.minusDays(1), diana);
        // Abierta y todavía dentro del plazo, sin técnico.
        Solicitud enPlazo = solicitud(4L, EstadoNombre.PENDIENTE, AHORA.minusDays(1), AHORA.plusDays(6), null);

        when(areas.findById(1L)).thenReturn(Optional.of(mantenimiento));
        when(solicitudes.registradasEntre(any(), any(), eq(1L))).thenReturn(List.of(aTiempo, tarde, vencida, enPlazo));
        when(historial.fechasDeResolucion(List.of(1L, 2L, 3L, 4L))).thenReturn(List.of(
                movimiento(1L, AHORA.minusDays(5).plusHours(36)),
                movimiento(2L, AHORA.minusDays(4))));

        ReporteSolicitudesResponse r = servicio.generar(supervisor, null, null, null);

        assertThat(r.alcance()).isEqualTo("Mantenimiento general");
        assertThat(r.desde()).isEqualTo(LocalDate.of(2026, 10, 28));
        assertThat(r.hasta()).isEqualTo(LocalDate.of(2026, 11, 26));
        assertThat(r.filas()).extracting(FilaReporte::cumplimiento)
                .containsExactly(FilaReporte.A_TIEMPO, FilaReporte.FUERA_DE_PLAZO, FilaReporte.VENCIDA, FilaReporte.EN_PLAZO);
        assertThat(r.filas().get(0).diasAtencion()).isEqualByComparingTo("1.5");
        assertThat(r.filas().get(1).diasAtencion()).isEqualByComparingTo("6.0");
        assertThat(r.filas().get(2).fechaResolucion()).isNull();

        assertThat(r.resumen().total()).isEqualTo(4);
        assertThat(r.resumen().atendidas()).isEqualTo(2);
        assertThat(r.resumen().abiertas()).isEqualTo(2);
        assertThat(r.resumen().vencidas()).isEqualTo(1);
        assertThat(r.resumen().atendidasFueraDePlazo()).isEqualTo(1);
        assertThat(r.resumen().cumplimientoSla()).isEqualByComparingTo("50.0");
        assertThat(r.resumen().mttrDias()).isEqualByComparingTo("3.8");
        assertThat(r.resumen().porEstado()).extracting("nombre")
                .containsExactly("Pendiente", "En proceso", "Resuelto", "Cerrado");

        assertThat(r.cargaTecnica()).hasSize(1);
        assertThat(r.cargaTecnica().get(0).tecnico()).isEqualTo("Diana Cárdenas");
        assertThat(r.cargaTecnica().get(0).enProceso()).isEqualTo(1);
        assertThat(r.cargaTecnica().get(0).resueltas()).isEqualTo(2);
    }

    @Test
    void elSupervisorSoloVeSuAreaAunquePidaOtra() {
        when(areas.findById(1L)).thenReturn(Optional.of(mantenimiento));
        when(solicitudes.registradasEntre(any(), any(), eq(1L))).thenReturn(List.of());

        ReporteSolicitudesResponse r = servicio.generar(supervisor, null, null, 99L);

        assertThat(r.filas()).isEmpty();
        assertThat(r.resumen().cumplimientoSla()).isNull();
        assertThat(r.resumen().mttrDias()).isNull();
        verify(solicitudes).registradasEntre(any(), any(), eq(1L));
        verifyNoInteractions(historial);
    }

    @Test
    void elAdministradorPuedeVerTodasLasAreas() {
        when(solicitudes.registradasEntre(
                LocalDate.of(2026, 11, 1).atStartOfDay(), LocalDate.of(2026, 12, 1).atStartOfDay(), null))
                .thenReturn(List.of());

        ReporteSolicitudesResponse r = servicio.generar(
                admin, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null);

        assertThat(r.alcance()).isEqualTo("Todas las áreas");
        verify(solicitudes).registradasEntre(any(), any(), isNull());
    }

    @Test
    void rechazaRangosInvalidos() {
        assertThatThrownBy(() -> servicio.generar(admin, LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1), null))
                .isInstanceOf(FieldValidationException.class)
                .hasMessageContaining("posterior");
        assertThatThrownBy(() -> servicio.generar(admin, LocalDate.of(2025, 1, 1), LocalDate.of(2026, 11, 1), null))
                .isInstanceOf(FieldValidationException.class)
                .hasMessageContaining("un año");
        verify(solicitudes, org.mockito.Mockito.never()).registradasEntre(any(), any(), any());
    }

    @Test
    void otrosRolesNoGeneranReportes() {
        UsuarioAutenticado estudiante = new UsuarioAutenticado(1L, "Andrea", RolNombre.ESTUDIANTE, null);
        UsuarioAutenticado supervisorSinArea = new UsuarioAutenticado(2L, "Sin área", RolNombre.SUPERVISOR, null);

        assertThatThrownBy(() -> servicio.generar(estudiante, null, null, null)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> servicio.generar(supervisorSinArea, null, null, null))
                .isInstanceOf(ForbiddenException.class);
        verify(solicitudes, org.mockito.Mockito.never()).registradasEntre(any(), any(), any());
        verify(areas, org.mockito.Mockito.never()).findById(anyLong());
    }

    @Test
    void losDiasDeAtencionSeRedondeanAUnDecimal() {
        assertThat(ReporteService.dias(AHORA, AHORA.plusHours(10))).isEqualByComparingTo(new BigDecimal("0.4"));
        assertThat(ReporteService.dias(AHORA, AHORA.minusHours(1))).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private static Solicitud solicitud(Long id, String estado, LocalDateTime creacion, LocalDateTime vencimiento,
                                       Usuario tecnico) {
        int orden = List.of(EstadoNombre.PENDIENTE, EstadoNombre.EN_REVISION, EstadoNombre.ASIGNADO,
                EstadoNombre.EN_PROCESO, EstadoNombre.RESUELTO, EstadoNombre.CERRADO).indexOf(estado) + 1;
        return Solicitud.builder()
                .id(id)
                .codigo("SOL-2026-000" + id)
                .area(Area.builder().id(1L).nombre("Mantenimiento general").build())
                .categoria(Categoria.builder().nombre("Mantenimiento").build())
                .prioridad(Prioridad.builder().nombre("Alta").slaDias(3).build())
                .estado(Estado.builder().nombre(estado).orden(orden).build())
                .tecnico(tecnico)
                .fechaCreacion(creacion)
                .fechaVencimiento(vencimiento)
                .build();
    }

    private static UltimoMovimiento movimiento(Long solicitudId, LocalDateTime fecha) {
        return new UltimoMovimiento() {
            @Override
            public Long getSolicitudId() {
                return solicitudId;
            }

            @Override
            public LocalDateTime getFecha() {
                return fecha;
            }
        };
    }
}
