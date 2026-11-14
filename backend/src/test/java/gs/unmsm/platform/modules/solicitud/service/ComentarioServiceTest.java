package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.exception.BusinessRuleException;
import gs.unmsm.platform.common.exception.ResourceNotFoundException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.notificacion.service.NotificacionService;
import gs.unmsm.platform.modules.solicitud.dto.CrearComentarioRequest;
import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.model.Comentario;
import gs.unmsm.platform.modules.trazabilidad.repository.ComentarioRepository;
import gs.unmsm.platform.modules.usuario.model.Rol;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    private static final String CODIGO = "SOL-2026-0001";
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 14, 12, 0);

    @Mock SolicitudRepository solicitudes;
    @Mock ComentarioRepository comentarios;
    @Mock UsuarioRepository usuarios;
    @Mock NotificacionService notificaciones;

    private ComentarioService servicio;
    private final Usuario andrea = Usuario.builder()
            .id(1L)
            .nombres("Andrea Mendoza")
            .rol(Rol.builder().nombre(RolNombre.ESTUDIANTE).build())
            .activo(true)
            .build();
    private final UsuarioAutenticado sesion =
            new UsuarioAutenticado(1L, "Andrea Mendoza", RolNombre.ESTUDIANTE, null);

    @BeforeEach
    void crearServicio() {
        Clock reloj = Clock.fixed(AHORA.atZone(LIMA).toInstant(), LIMA);
        servicio = new ComentarioService(solicitudes, comentarios, usuarios, notificaciones, reloj);
    }

    @Test
    void publicaUnComentarioEnUnaSolicitudAbierta() {
        Solicitud solicitud = solicitudEn(EstadoNombre.PENDIENTE);
        when(solicitudes.findByCodigo(CODIGO)).thenReturn(Optional.of(solicitud));
        when(usuarios.findById(1L)).thenReturn(Optional.of(andrea));

        servicio.comentar(sesion, CODIGO, new CrearComentarioRequest("¿Ya revisaron el aula?"));

        ArgumentCaptor<Comentario> guardado = ArgumentCaptor.forClass(Comentario.class);
        verify(comentarios).save(guardado.capture());
        assertThat(guardado.getValue().getContenido()).isEqualTo("¿Ya revisaron el aula?");
        assertThat(guardado.getValue().getAutor()).isSameAs(andrea);
        assertThat(guardado.getValue().getSolicitud()).isSameAs(solicitud);
        assertThat(guardado.getValue().getFechaCreacion()).isEqualTo(AHORA);
        verify(notificaciones).avisarComentario(solicitud, andrea);
    }

    @Test
    void noPermiteComentarUnaSolicitudCerrada() {
        when(solicitudes.findByCodigo(CODIGO)).thenReturn(Optional.of(solicitudEn(EstadoNombre.CERRADO)));

        assertThatThrownBy(() -> servicio.comentar(sesion, CODIGO, new CrearComentarioRequest("Hola")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No puedes comentar una solicitud cerrada.");

        verify(comentarios, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unaSolicitudAjenaRespondeComoNoEncontrada() {
        Solicitud ajena = solicitudEn(EstadoNombre.PENDIENTE);
        ajena.setSolicitante(Usuario.builder().id(99L).build());
        when(solicitudes.findByCodigo(CODIGO)).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> servicio.comentar(sesion, CODIGO, new CrearComentarioRequest("Hola")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Solicitud solicitudEn(String estado) {
        return Solicitud.builder()
                .id(10L)
                .codigo(CODIGO)
                .solicitante(andrea)
                .estado(Estado.builder().nombre(estado).build())
                .build();
    }
}
