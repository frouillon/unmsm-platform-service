package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.exception.BusinessRuleException;
import gs.unmsm.platform.common.exception.FieldValidationException;
import gs.unmsm.platform.common.exception.ForbiddenException;
import gs.unmsm.platform.common.exception.ResourceNotFoundException;
import gs.unmsm.platform.common.exception.UnauthorizedException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.dto.AsignarSolicitudRequest;
import gs.unmsm.platform.modules.solicitud.dto.CambiarEstadoRequest;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudGestionResumenResponse;
import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
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
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudGestionService {

    private static final String NO_ENCONTRADA = "No encontramos la solicitud o no tienes acceso a ella.";

    private final SolicitudRepository solicitudes;
    private final UsuarioRepository usuarios;
    private final EstadoRepository estados;
    private final HistorialEstadoRepository historial;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<SolicitudGestionResumenResponse> listarAsignadas(UsuarioAutenticado usuario) {
        if (!RolNombre.TECNICO.equals(usuario.rol())) {
            throw new ForbiddenException("No tienes permisos para ver esta bandeja.");
        }
        List<Solicitud> lista = solicitudes.findByTecnicoIdOrderByFechaCreacionDescIdDesc(usuario.id());
        return mapear(lista);
    }

    @Transactional(readOnly = true)
    public List<SolicitudGestionResumenResponse> listarPorArea(UsuarioAutenticado usuario) {
        if (!(RolNombre.SUPERVISOR.equals(usuario.rol()) || RolNombre.ADMINISTRADOR.equals(usuario.rol()))) {
            throw new ForbiddenException("No tienes permisos para ver esta bandeja.");
        }
        List<Solicitud> lista;
        if (RolNombre.ADMINISTRADOR.equals(usuario.rol())) {
            lista = solicitudes.findAllByOrderByFechaCreacionDescIdDesc();
        } else {
            if (usuario.areaId() == null) {
                throw new ForbiddenException("Tu usuario no tiene un área asignada.");
            }
            lista = solicitudes.findByAreaIdOrderByFechaCreacionDescIdDesc(usuario.areaId());
        }
        return mapear(lista);
    }

    @Transactional
    public void asignar(String codigo, AsignarSolicitudRequest request, UsuarioAutenticado usuario) {
        if (!(RolNombre.SUPERVISOR.equals(usuario.rol()) || RolNombre.ADMINISTRADOR.equals(usuario.rol()))) {
            throw new ForbiddenException("Solo un supervisor o administrador puede asignar solicitudes.");
        }
        Solicitud solicitud = cargarGestionable(usuario, codigo);
        Usuario tecnico = usuarios.findById(request.tecnicoId())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new FieldValidationException("tecnicoId", "El técnico seleccionado no está disponible."));
        if (!RolNombre.TECNICO.equals(tecnico.getRol().getNombre())) {
            throw new FieldValidationException("tecnicoId", "El usuario seleccionado debe tener rol TÉCNICO.");
        }
        if (!Objects.equals(tecnico.getArea().getId(), solicitud.getArea().getId())) {
            throw new BusinessRuleException("El técnico debe pertenecer al área responsable de la solicitud.");
        }
        if (solicitud.getTecnico() != null && Objects.equals(solicitud.getTecnico().getId(), tecnico.getId())) {
            // idempotente, sin cambio
            return;
        }

        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Estado estadoActual = solicitud.getEstado();
        Estado enProceso = estados.findByNombre(EstadoNombre.EN_PROCESO)
                .orElseThrow(() -> new IllegalStateException("Falta el estado " + EstadoNombre.EN_PROCESO));

        solicitud.setTecnico(tecnico);
        if (EstadoNombre.PENDIENTE.equals(estadoActual.getNombre())) {
            solicitud.setEstado(enProceso);
            historial.save(HistorialEstado.builder()
                    .solicitud(solicitud)
                    .estadoOrigen(estadoActual)
                    .estadoDestino(enProceso)
                    .usuario(obtenerUsuario(usuario))
                    .rolUsuario(usuario.rol())
                    .notaTecnica(StringUtils.hasText(request.notaTecnica()) ? request.notaTecnica().trim() : "Solicitud asignada a " + tecnico.getNombres() + ".")
                    .fechaRegistro(ahora)
                    .build());
        } else {
            historial.save(HistorialEstado.builder()
                    .solicitud(solicitud)
                    .estadoOrigen(estadoActual)
                    .estadoDestino(estadoActual)
                    .usuario(obtenerUsuario(usuario))
                    .rolUsuario(usuario.rol())
                    .notaTecnica(StringUtils.hasText(request.notaTecnica()) ? request.notaTecnica().trim() : "Solicitud reasignada a " + tecnico.getNombres() + ".")
                    .fechaRegistro(ahora)
                    .build());
        }
        solicitudes.save(solicitud);
    }

    @Transactional
    public void cambiarEstado(String codigo, CambiarEstadoRequest request, UsuarioAutenticado usuario) {
        Solicitud solicitud = cargarGestionable(usuario, codigo);
        String estadoOrigen = solicitud.getEstado().getNombre();
        String estadoDestino = request.estadoDestino();

        if (!TransicionEstados.permitido(estadoOrigen, usuario.rol(), estadoDestino)) {
            throw new BusinessRuleException("No puedes cambiar de '" + estadoOrigen + "' a '" + estadoDestino + "' con tu rol.");
        }
        if (RolNombre.TECNICO.equals(usuario.rol())) {
            if (solicitud.getTecnico() == null || !Objects.equals(solicitud.getTecnico().getId(), usuario.id())) {
                throw new ForbiddenException("Solo el técnico asignado puede atender esta solicitud.");
            }
        }

        if (TransicionEstados.requiereNotaTecnica(estadoDestino)) {
            if (!StringUtils.hasText(request.notaTecnica())) {
                throw new FieldValidationException("notaTecnica", "Debes indicar un comentario al resolver o cerrar la solicitud.");
            }
        }

        Estado destino = estados.findByNombre(estadoDestino)
                .orElseThrow(() -> new FieldValidationException("estadoDestino", "El estado destino no existe."));

        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Estado origen = solicitud.getEstado();
        solicitud.setEstado(destino);

        String nota = StringUtils.hasText(request.notaTecnica()) ? request.notaTecnica().trim() : null;
        historial.save(HistorialEstado.builder()
                .solicitud(solicitud)
                .estadoOrigen(origen)
                .estadoDestino(destino)
                .usuario(obtenerUsuario(usuario))
                .rolUsuario(usuario.rol())
                .notaTecnica(nota)
                .fechaRegistro(ahora)
                .build());
        solicitudes.save(solicitud);
    }

    private Solicitud cargarGestionable(UsuarioAutenticado usuario, String codigo) {
        Solicitud solicitud = solicitudes.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADA));
        if (RolNombre.ADMINISTRADOR.equals(usuario.rol())) {
            return solicitud;
        }
        if (RolNombre.SUPERVISOR.equals(usuario.rol())) {
            if (usuario.areaId() == null || !Objects.equals(solicitud.getArea().getId(), usuario.areaId())) {
                throw new ResourceNotFoundException(NO_ENCONTRADA);
            }
            return solicitud;
        }
        if (RolNombre.TECNICO.equals(usuario.rol())) {
            if (solicitud.getTecnico() == null || !Objects.equals(solicitud.getTecnico().getId(), usuario.id())) {
                throw new ResourceNotFoundException(NO_ENCONTRADA);
            }
            return solicitud;
        }
        throw new ForbiddenException("No tienes permisos para gestionar solicitudes.");
    }

    private Usuario obtenerUsuario(UsuarioAutenticado usuario) {
        return usuarios.findById(usuario.id())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new UnauthorizedException("Tu sesión ya no es válida. Inicia sesión nuevamente."));
    }

    private List<SolicitudGestionResumenResponse> mapear(List<Solicitud> lista) {
        if (lista.isEmpty()) {
            return List.of();
        }
        Map<Long, LocalDateTime> ultimos = historial.ultimosMovimientos(lista.stream().map(Solicitud::getId).toList())
                .stream().collect(Collectors.toMap(HistorialEstadoRepository.UltimoMovimiento::getSolicitudId,
                        HistorialEstadoRepository.UltimoMovimiento::getFecha));
        return lista.stream().map(s -> SolicitudGestionResumenResponse.from(s, ultimos.get(s.getId()))).toList();
    }
}
