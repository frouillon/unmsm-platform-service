package gs.unmsm.platform.modules.dashboard.service;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.dashboard.dto.DashboardResponse;
import gs.unmsm.platform.modules.dashboard.repository.DashboardRepository;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 14, 12, 0);

    @Mock DashboardRepository dashboard;

    private DashboardService servicio;

    @BeforeEach
    void crearServicio() {
        Clock reloj = Clock.fixed(AHORA.atZone(LIMA).toInstant(), LIMA);
        servicio = new DashboardService(dashboard, reloj);
    }

    @Test
    void elEstudianteConsultaSoloSusSolicitudes() {
        when(dashboard.total(eq(10L), isNull(), isNull())).thenReturn(5L);
        when(dashboard.vencidas(eq(10L), isNull(), isNull(), eq(AHORA))).thenReturn(1L);
        when(dashboard.porEstado(eq(10L), isNull(), isNull()))
                .thenReturn(List.<Object[]>of(new Object[]{"Pendiente", 3L}, new Object[]{"Resuelto", 2L}));
        when(dashboard.porArea(eq(10L), isNull(), isNull()))
                .thenReturn(List.<Object[]>of(new Object[]{"Mantenimiento", 5L}));
        when(dashboard.porPrioridad(eq(10L), isNull(), isNull()))
                .thenReturn(List.<Object[]>of(new Object[]{"Media", 5L}));

        DashboardResponse r = servicio.indicadores(
                new UsuarioAutenticado(10L, "Andrea", RolNombre.ESTUDIANTE, null));

        assertThat(r.total()).isEqualTo(5);
        assertThat(r.vencidas()).isEqualTo(1);
        assertThat(r.porEstado()).extracting("nombre").containsExactly("Pendiente", "Resuelto");
        assertThat(r.porEstado()).extracting("cantidad").containsExactly(3L, 2L);
        verify(dashboard).total(10L, null, null);
    }

    @Test
    void elSupervisorSinAreaDevuelveVacío() {
        DashboardResponse r = servicio.indicadores(
                new UsuarioAutenticado(20L, "Ronald", RolNombre.SUPERVISOR, null));

        assertThat(r.total()).isZero();
        assertThat(r.porEstado()).isEmpty();
    }

    @Test
    void elAdministradorConsultaSinFiltros() {
        when(dashboard.total(isNull(), isNull(), isNull())).thenReturn(20L);
        when(dashboard.vencidas(isNull(), isNull(), isNull(), any())).thenReturn(0L);
        when(dashboard.porEstado(isNull(), isNull(), isNull())).thenReturn(List.of());
        when(dashboard.porArea(isNull(), isNull(), isNull())).thenReturn(List.of());
        when(dashboard.porPrioridad(isNull(), isNull(), isNull())).thenReturn(List.of());

        DashboardResponse r = servicio.indicadores(
                new UsuarioAutenticado(1L, "Admin", RolNombre.ADMINISTRADOR, null));

        assertThat(r.total()).isEqualTo(20);
        verify(dashboard).total(null, null, null);
    }
}
