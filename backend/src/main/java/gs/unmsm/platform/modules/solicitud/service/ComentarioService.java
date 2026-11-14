package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.exception.BusinessRuleException;
import gs.unmsm.platform.common.exception.ResourceNotFoundException;
import gs.unmsm.platform.common.exception.UnauthorizedException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.dto.CrearComentarioRequest;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.model.Comentario;
import gs.unmsm.platform.modules.trazabilidad.repository.ComentarioRepository;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final SolicitudRepository solicitudes;
    private final ComentarioRepository comentarios;
    private final UsuarioRepository usuarios;
    private final Clock clock;

    @Transactional
    public void comentar(UsuarioAutenticado autenticado, String codigo, CrearComentarioRequest request) {
        Solicitud solicitud = solicitudes.findByCodigo(codigo)
                .filter(s -> SolicitudAcceso.puedeVer(autenticado, s))
                .orElseThrow(() -> new ResourceNotFoundException(SolicitudConsultaService.NO_ENCONTRADA));

        if (EstadoNombre.CERRADO.equals(solicitud.getEstado().getNombre())) {
            throw new BusinessRuleException("No puedes comentar una solicitud cerrada.");
        }

        Usuario autor = usuarios.findById(autenticado.id())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new UnauthorizedException("Tu sesión ya no es válida. Inicia sesión nuevamente."));

        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        comentarios.save(Comentario.builder()
                .solicitud(solicitud)
                .autor(autor)
                .contenido(request.contenido())
                .fechaCreacion(ahora)
                .build());
    }
}
