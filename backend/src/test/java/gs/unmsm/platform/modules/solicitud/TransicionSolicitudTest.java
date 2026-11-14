package gs.unmsm.platform.modules.solicitud;

import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.service.TransicionSolicitud;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransicionSolicitudTest {

    @Test
    void cadaRolSoloAvanzaSuTramo() {
        assertThat(TransicionSolicitud.siguiente(RolNombre.SUPERVISOR, EstadoNombre.PENDIENTE))
                .hasValueSatisfying(p -> {
                    assertThat(p.destino()).isEqualTo(EstadoNombre.EN_REVISION);
                    assertThat(p.requiereTecnico()).isFalse();
                });
        assertThat(TransicionSolicitud.siguiente(RolNombre.SUPERVISOR, EstadoNombre.EN_REVISION))
                .hasValueSatisfying(p -> {
                    assertThat(p.destino()).isEqualTo(EstadoNombre.ASIGNADO);
                    assertThat(p.requiereTecnico()).isTrue();
                });
        assertThat(TransicionSolicitud.siguiente(RolNombre.TECNICO, EstadoNombre.ASIGNADO))
                .hasValueSatisfying(p -> assertThat(p.destino()).isEqualTo(EstadoNombre.EN_PROCESO));
        assertThat(TransicionSolicitud.siguiente(RolNombre.TECNICO, EstadoNombre.EN_PROCESO))
                .hasValueSatisfying(p -> assertThat(p.destino()).isEqualTo(EstadoNombre.RESUELTO));
        assertThat(TransicionSolicitud.siguiente(RolNombre.ESTUDIANTE, EstadoNombre.RESUELTO))
                .hasValueSatisfying(p -> assertThat(p.destino()).isEqualTo(EstadoNombre.CERRADO));

        assertThat(TransicionSolicitud.siguiente(RolNombre.SUPERVISOR, EstadoNombre.EN_PROCESO))
                .hasValueSatisfying(p -> {
                    assertThat(p.destino()).isEqualTo(EstadoNombre.RESUELTO);
                    assertThat(p.requiereNota()).isTrue();
                });
        assertThat(TransicionSolicitud.siguiente(RolNombre.SUPERVISOR, EstadoNombre.RESUELTO))
                .hasValueSatisfying(p -> assertThat(p.destino()).isEqualTo(EstadoNombre.CERRADO));

        assertThat(TransicionSolicitud.siguiente(RolNombre.ESTUDIANTE, EstadoNombre.PENDIENTE)).isEmpty();
        assertThat(TransicionSolicitud.siguiente(RolNombre.SUPERVISOR, EstadoNombre.ASIGNADO)).isEmpty();
        assertThat(TransicionSolicitud.siguiente(RolNombre.TECNICO, EstadoNombre.RESUELTO)).isEmpty();
        assertThat(TransicionSolicitud.siguiente(RolNombre.ADMINISTRADOR, EstadoNombre.PENDIENTE))
                .hasValueSatisfying(p -> assertThat(p.destino()).isEqualTo(EstadoNombre.EN_REVISION));
        assertThat(TransicionSolicitud.siguiente(RolNombre.ADMINISTRADOR, EstadoNombre.CERRADO)).isEmpty();
    }
}
