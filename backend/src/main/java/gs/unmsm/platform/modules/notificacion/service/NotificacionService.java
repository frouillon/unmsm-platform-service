package gs.unmsm.platform.modules.notificacion.service;

import gs.unmsm.platform.common.exception.ResourceNotFoundException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.notificacion.dto.NotificacionResponse;
import gs.unmsm.platform.modules.notificacion.model.Notificacion;
import gs.unmsm.platform.modules.notificacion.repository.NotificacionRepository;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    public static final String TIPO_CAMBIO_ESTADO = "CAMBIO_ESTADO";
    public static final String TIPO_ASIGNACION = "ASIGNACION";
    public static final String TIPO_COMENTARIO = "COMENTARIO";

    private final NotificacionRepository notificaciones;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar(UsuarioAutenticado usuario) {
        return notificaciones.findByUsuarioIdOrderByFechaCreacionDescIdDesc(usuario.id()).stream()
                .map(this::mapear)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(UsuarioAutenticado usuario) {
        return notificaciones.countByUsuarioIdAndLeidaFalse(usuario.id());
    }

    @Transactional
    public void marcarLeida(UsuarioAutenticado usuario, Long id) {
        Notificacion n = notificaciones.findByIdAndUsuarioId(id, usuario.id())
                .orElseThrow(() -> new ResourceNotFoundException("No encontramos esa notificación."));
        n.setLeida(true);
    }

    @Transactional
    public int marcarTodasLeidas(UsuarioAutenticado usuario) {
        return notificaciones.marcarTodasLeidas(usuario.id());
    }

    @Transactional
    public void avisarCambioEstado(Solicitud solicitud, Usuario actor, String destino) {
        LocalDateTime ahora = ahora();
        Set<Long> avisados = new LinkedHashSet<>();

        if (EstadoNombre.ASIGNADO.equals(destino) && solicitud.getTecnico() != null) {
            crearSiCorresponde(solicitud.getTecnico(), actor, avisados, solicitud, TIPO_ASIGNACION,
                    "Te asignaron la solicitud %s.".formatted(solicitud.getCodigo()), ahora);
        }

        String mensajeEstado = "La solicitud %s pasó a %s.".formatted(solicitud.getCodigo(), destino);
        notificarInteresados(solicitud, actor, avisados, TIPO_CAMBIO_ESTADO, mensajeEstado, ahora);
    }

    @Transactional
    public void avisarComentario(Solicitud solicitud, Usuario autor) {
        String mensaje = "Nuevo comentario en la solicitud %s.".formatted(solicitud.getCodigo());
        notificarInteresados(solicitud, autor, new LinkedHashSet<>(), TIPO_COMENTARIO, mensaje, ahora());
    }

    private void notificarInteresados(
            Solicitud solicitud,
            Usuario actor,
            Set<Long> avisados,
            String tipo,
            String mensaje,
            LocalDateTime ahora) {
        crearSiCorresponde(solicitud.getSolicitante(), actor, avisados, solicitud, tipo, mensaje, ahora);
        if (solicitud.getTecnico() != null) {
            crearSiCorresponde(solicitud.getTecnico(), actor, avisados, solicitud, tipo, mensaje, ahora);
        }
    }

    private void crearSiCorresponde(
            Usuario destinatario,
            Usuario actor,
            Set<Long> avisados,
            Solicitud solicitud,
            String tipo,
            String mensaje,
            LocalDateTime ahora) {
        if (destinatario == null || destinatario.getId() == null) {
            return;
        }
        if (actor != null && Objects.equals(destinatario.getId(), actor.getId())) {
            return;
        }
        if (!avisados.add(destinatario.getId())) {
            return;
        }
        notificaciones.save(Notificacion.builder()
                .usuario(destinatario)
                .solicitud(solicitud)
                .tipo(tipo)
                .mensaje(recortar(mensaje))
                .leida(false)
                .fechaCreacion(ahora)
                .build());
    }

    private NotificacionResponse mapear(Notificacion n) {
        return new NotificacionResponse(
                n.getId(),
                n.getTipo(),
                n.getMensaje(),
                n.getSolicitud().getCodigo(),
                Boolean.TRUE.equals(n.getLeida()),
                n.getFechaCreacion());
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

    private static String recortar(String mensaje) {
        return mensaje.length() <= 255 ? mensaje : mensaje.substring(0, 252) + "...";
    }
}
