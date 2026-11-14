package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF-19 · HU-09 a HU-13 — Máquina de estados de la solicitud.
 * <p>
 * Técnica: transición de estados (estado final y condición de cada transición)
 * y partición de equivalencia (rol que no pertenece al sistema).
 */
class TransicionSolicitudReglasTest {

    private static final List<String> ROLES = List.of(
            RolNombre.ESTUDIANTE, RolNombre.TECNICO, RolNombre.SUPERVISOR, RolNombre.ADMINISTRADOR);

    private static final List<String> ESTADOS = List.of(
            EstadoNombre.PENDIENTE, EstadoNombre.EN_REVISION, EstadoNombre.ASIGNADO,
            EstadoNombre.EN_PROCESO, EstadoNombre.RESUELTO, EstadoNombre.CERRADO);

    @Test
    void cerradoEsElEstadoFinalParaTodosLosRoles() {
        for (String rol : ROLES) {
            assertThat(TransicionSolicitud.siguiente(rol, EstadoNombre.CERRADO))
                    .as("%s no debe poder mover una solicitud cerrada", rol)
                    .isEmpty();
        }
    }

    @Test
    void unRolDesconocidoNoPuedeAvanzarDesdeNingunEstado() {
        for (String estado : ESTADOS) {
            assertThat(TransicionSolicitud.siguiente("INVITADO", estado))
                    .as("un rol desconocido no debe avanzar desde %s", estado)
                    .isEmpty();
        }
    }

    @Test
    void soloElPasoHaciaAsignadoExigeElegirTecnico() {
        assertThat(TransicionSolicitud.siguiente(RolNombre.SUPERVISOR, EstadoNombre.EN_REVISION))
                .contains(new TransicionSolicitud.Paso(EstadoNombre.ASIGNADO, true));

        for (String rol : ROLES) {
            for (String estado : ESTADOS) {
                TransicionSolicitud.siguiente(rol, estado).ifPresent(paso ->
                        assertThat(paso.requiereTecnico())
                                .as("%s: %s → %s", rol, estado, paso.destino())
                                .isEqualTo(EstadoNombre.ASIGNADO.equals(paso.destino())));
            }
        }
    }
}
