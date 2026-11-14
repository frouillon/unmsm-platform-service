package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.usuario.model.Area;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-08 · RNF-14 — Reglas de autorización: quién puede ver una solicitud.
 * <p>
 * Técnica: tabla de decisión (rol del usuario × su relación con la solicitud).
 */
class SolicitudAccesoTest {

    private static final long AREA_MANTENIMIENTO = 1L;
    private static final long AREA_TI = 2L;

    private static final long ANDREA = 10L;
    private static final long LUIS = 11L;
    private static final long DIANA = 20L;
    private static final long CARLOS = 21L;

    /** Solicitud de Andrea, del área de Mantenimiento y asignada a la técnica Diana. */
    private final Solicitud solicitud = Solicitud.builder()
            .solicitante(usuario(ANDREA))
            .tecnico(usuario(DIANA))
            .area(Area.builder().id(AREA_MANTENIMIENTO).build())
            .build();

    @Test
    void elEstudianteSoloVeSusPropiasSolicitudes() {
        assertThat(SolicitudAcceso.puedeVer(sesion(ANDREA, RolNombre.ESTUDIANTE, null), solicitud)).isTrue();
        assertThat(SolicitudAcceso.puedeVer(sesion(LUIS, RolNombre.ESTUDIANTE, null), solicitud)).isFalse();
    }

    @Test
    void elTecnicoSoloVeLasSolicitudesQueTieneAsignadas() {
        Solicitud sinAsignar = Solicitud.builder()
                .solicitante(usuario(ANDREA))
                .area(Area.builder().id(AREA_MANTENIMIENTO).build())
                .build();

        assertThat(SolicitudAcceso.puedeVer(sesion(DIANA, RolNombre.TECNICO, AREA_MANTENIMIENTO), solicitud)).isTrue();
        assertThat(SolicitudAcceso.puedeVer(sesion(CARLOS, RolNombre.TECNICO, AREA_TI), solicitud)).isFalse();
        assertThat(SolicitudAcceso.puedeVer(sesion(DIANA, RolNombre.TECNICO, AREA_MANTENIMIENTO), sinAsignar)).isFalse();
    }

    @Test
    void elSupervisorVeLasSolicitudesDeSuAreaYNoLasDeOtra() {
        assertThat(SolicitudAcceso.puedeVer(sesion(30L, RolNombre.SUPERVISOR, AREA_MANTENIMIENTO), solicitud)).isTrue();
        assertThat(SolicitudAcceso.puedeVer(sesion(31L, RolNombre.SUPERVISOR, AREA_TI), solicitud)).isFalse();
    }

    @Test
    void elAdministradorVeCualquierSolicitud() {
        assertThat(SolicitudAcceso.puedeVer(sesion(40L, RolNombre.ADMINISTRADOR, AREA_TI), solicitud)).isTrue();
        assertThat(SolicitudAcceso.puedeVer(sesion(41L, RolNombre.ADMINISTRADOR, null), solicitud)).isTrue();
    }

    private static Usuario usuario(long id) {
        return Usuario.builder().id(id).build();
    }

    private static UsuarioAutenticado sesion(long id, String rol, Long areaId) {
        return new UsuarioAutenticado(id, "Usuario de prueba", rol, areaId);
    }
}
