package gs.unmsm.platform.modules.notificacion.service;

import gs.unmsm.platform.common.exception.ResourceNotFoundException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.notificacion.dto.NotificacionResponse;
import gs.unmsm.platform.modules.notificacion.model.Notificacion;
import gs.unmsm.platform.modules.notificacion.repository.NotificacionRepository;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    private static final String CODIGO = "SOL-2026-0001";
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 14, 12, 0);

    @Mock NotificacionRepository notificaciones;

    private NotificacionService servicio;

    private final Usuario andrea = Usuario.builder().id(1L).nombres("Andrea Mendoza").build();
    private final Usuario diana = Usuario.builder().id(2L).nombres("Diana Cárdenas").build();
    private final Usuario ronald = Usuario.builder().id(3L).nombres("Ronald Munayco").build();
    private final UsuarioAutenticado sesionAndrea =
            new UsuarioAutenticado(1L, "Andrea Mendoza", RolNombre.ESTUDIANTE, null);

    @BeforeEach
    void crearServicio() {
        Clock reloj = Clock.fixed(AHORA.atZone(LIMA).toInstant(), LIMA);
        servicio = new NotificacionService(notificaciones, reloj);
    }

    @Test
    void alAsignarAvisaAlTecnicoYAlSolicitanteSinDuplicar() {
        Solicitud solicitud = Solicitud.builder()
                .codigo(CODIGO)
                .solicitante(andrea)
                .tecnico(diana)
                .build();

        servicio.avisarCambioEstado(solicitud, ronald, EstadoNombre.ASIGNADO);

        ArgumentCaptor<Notificacion> guardadas = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificaciones, times(2)).save(guardadas.capture());
        assertThat(guardadas.getAllValues()).extracting(Notificacion::getTipo)
                .containsExactly(NotificacionService.TIPO_ASIGNACION, NotificacionService.TIPO_CAMBIO_ESTADO);
        assertThat(guardadas.getAllValues()).extracting(n -> n.getUsuario().getId())
                .containsExactly(2L, 1L);
        assertThat(guardadas.getAllValues().get(0).getMensaje())
                .isEqualTo("Te asignaron la solicitud SOL-2026-0001.");
        assertThat(guardadas.getAllValues().get(1).getMensaje())
                .isEqualTo("La solicitud SOL-2026-0001 pasó a Asignado.");
    }

    @Test
    void elActorNoRecibeAvisoDeSuPropioComentario() {
        Solicitud solicitud = Solicitud.builder()
                .codigo(CODIGO)
                .solicitante(andrea)
                .tecnico(diana)
                .build();

        servicio.avisarComentario(solicitud, andrea);

        ArgumentCaptor<Notificacion> guardada = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificaciones).save(guardada.capture());
        assertThat(guardada.getValue().getUsuario()).isSameAs(diana);
        assertThat(guardada.getValue().getTipo()).isEqualTo(NotificacionService.TIPO_COMENTARIO);
        assertThat(guardada.getValue().getMensaje())
                .isEqualTo("Nuevo comentario en la solicitud SOL-2026-0001.");
    }

    @Test
    void listarYMarcarComoLeidaSoloAfectanAlDueno() {
        Notificacion propia = Notificacion.builder()
                .id(10L)
                .usuario(andrea)
                .solicitud(Solicitud.builder().codigo(CODIGO).build())
                .tipo(NotificacionService.TIPO_CAMBIO_ESTADO)
                .mensaje("Aviso")
                .leida(false)
                .fechaCreacion(AHORA)
                .build();
        when(notificaciones.findByUsuarioIdOrderByFechaCreacionDescIdDesc(1L)).thenReturn(List.of(propia));
        when(notificaciones.findByIdAndUsuarioId(10L, 1L)).thenReturn(Optional.of(propia));
        when(notificaciones.findByIdAndUsuarioId(99L, 1L)).thenReturn(Optional.empty());

        List<NotificacionResponse> lista = servicio.listar(sesionAndrea);
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).codigoSolicitud()).isEqualTo(CODIGO);
        assertThat(lista.get(0).leida()).isFalse();

        servicio.marcarLeida(sesionAndrea, 10L);
        assertThat(propia.getLeida()).isTrue();

        assertThatThrownBy(() -> servicio.marcarLeida(sesionAndrea, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(notificaciones, never()).save(any());
    }
}
