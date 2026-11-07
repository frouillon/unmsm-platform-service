package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.exception.BusinessRuleException;
import gs.unmsm.platform.common.exception.FieldValidationException;
import gs.unmsm.platform.common.exception.ResourceNotFoundException;
import gs.unmsm.platform.common.exception.UnauthorizedException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.dto.AvanzarSolicitudRequest;
import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.EstadoRepository;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.model.HistorialEstado;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SolicitudFlujoService {

    private final SolicitudRepository solicitudes;
    private final EstadoRepository estados;
    private final UsuarioRepository usuarios;
    private final HistorialEstadoRepository historial;
    private final Clock clock;

    /** Cambia el estado, asigna al técnico cuando corresponde y deja el movimiento en el seguimiento. */
    @Transactional
    public void avanzar(UsuarioAutenticado autenticado, String codigo, AvanzarSolicitudRequest request) {
        Solicitud solicitud = solicitudes.findByCodigo(codigo)
                .filter(s -> SolicitudAcceso.puedeVer(autenticado, s))
                .orElseThrow(() -> new ResourceNotFoundException(SolicitudConsultaService.NO_ENCONTRADA));

        String origen = solicitud.getEstado().getNombre();
        TransicionSolicitud.Paso paso = TransicionSolicitud.siguiente(autenticado.rol(), origen)
                .filter(p -> p.destino().equals(request.estado()))
                .orElseThrow(() -> new BusinessRuleException(
                        "No puedes pasar esta solicitud de %s a %s.".formatted(origen, request.estado())));

        Usuario actor = usuarios.findById(autenticado.id())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new UnauthorizedException("Tu sesión ya no es válida. Inicia sesión nuevamente."));

        if (paso.requiereTecnico()) {
            solicitud.setTecnico(tecnicoDelArea(request.tecnicoId(), solicitud));
        }

        Estado destino = estados.findByNombre(paso.destino())
                .orElseThrow(() -> new IllegalStateException("Falta el estado " + paso.destino()));
        Estado estadoOrigen = solicitud.getEstado();
        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        solicitud.setEstado(destino);
        historial.save(HistorialEstado.builder()
                .solicitud(solicitud)
                .estadoOrigen(estadoOrigen)
                .estadoDestino(destino)
                .usuario(actor)
                .rolUsuario(autenticado.rol())
                .notaTecnica(request.nota())
                .fechaRegistro(ahora)
                .build());
    }

    private Usuario tecnicoDelArea(Long tecnicoId, Solicitud solicitud) {
        if (tecnicoId == null) {
            throw new FieldValidationException("tecnicoId", "Elige al técnico que la atenderá.");
        }
        return usuarios.findById(tecnicoId)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> RolNombre.TECNICO.equals(u.getRol().getNombre()))
                .filter(u -> u.getArea() != null && Objects.equals(u.getArea().getId(), solicitud.getArea().getId()))
                .orElseThrow(() -> new FieldValidationException("tecnicoId", "Elige un técnico activo de esta área."));
    }
}
