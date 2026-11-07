package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.exception.ResourceNotFoundException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudDetalleResponse;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudDetalleResponse.AccionResponse;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudDetalleResponse.EvidenciaResponse;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudDetalleResponse.MovimientoResponse;
import gs.unmsm.platform.modules.solicitud.dto.TecnicoResponse;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudResumenResponse;
import gs.unmsm.platform.modules.solicitud.model.Evidencia;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.EvidenciaRepository;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository.UltimoMovimiento;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudConsultaService {

    static final String NO_ENCONTRADA = "No encontramos la solicitud o no tienes acceso a ella.";

    private final SolicitudRepository solicitudes;
    private final HistorialEstadoRepository historial;
    private final EvidenciaRepository evidencias;
    private final EvidenciaStorage storage;
    private final UsuarioRepository usuarios;

    public record ArchivoEvidencia(Path ruta, String nombre, String tipoMime) {
    }

    public List<SolicitudResumenResponse> misSolicitudes(UsuarioAutenticado usuario) {
        List<Solicitud> propias = solicitudes.findBySolicitanteIdOrderByFechaCreacionDescIdDesc(usuario.id());
        if (propias.isEmpty()) {
            return List.of();
        }
        return resumir(propias);
    }

    public List<SolicitudResumenResponse> delArea(UsuarioAutenticado usuario) {
        if (usuario.areaId() == null) {
            return List.of();
        }
        return resumir(solicitudes.findByAreaIdOrderByFechaCreacionDescIdDesc(usuario.areaId()));
    }

    public List<SolicitudResumenResponse> asignadas(UsuarioAutenticado usuario) {
        return resumir(solicitudes.findByTecnicoIdOrderByFechaCreacionDescIdDesc(usuario.id()));
    }

    public List<SolicitudResumenResponse> todas() {
        return resumir(solicitudes.findAllByOrderByFechaCreacionDescIdDesc());
    }

    public List<TecnicoResponse> tecnicosDelArea(UsuarioAutenticado usuario) {
        if (usuario.areaId() == null) {
            return List.of();
        }
        return usuarios.findByRolNombreAndAreaIdAndActivoTrueOrderByNombresAsc(RolNombre.TECNICO, usuario.areaId())
                .stream()
                .map(t -> new TecnicoResponse(t.getId(), t.getNombres()))
                .toList();
    }

    /** Técnicos activos del área de la solicitud, para asignarla. */
    public List<TecnicoResponse> tecnicosDeSolicitud(UsuarioAutenticado usuario, String codigo) {
        Solicitud solicitud = visible(usuario, codigo);
        return usuarios.findByRolNombreAndAreaIdAndActivoTrueOrderByNombresAsc(RolNombre.TECNICO, solicitud.getArea().getId())
                .stream()
                .map(t -> new TecnicoResponse(t.getId(), t.getNombres()))
                .toList();
    }

    public SolicitudDetalleResponse detalle(UsuarioAutenticado usuario, String codigo) {
        Solicitud s = visible(usuario, codigo);
        List<EvidenciaResponse> adjuntos = evidencias.findBySolicitudIdOrderByIdAsc(s.getId()).stream()
                .map(e -> new EvidenciaResponse(e.getId(), e.getNombreArchivo(), e.getTipoMime(), e.getTamanoBytes()))
                .toList();
        List<MovimientoResponse> movimientos = historial.findBySolicitudIdOrderByFechaRegistroAscIdAsc(s.getId())
                .stream()
                .map(h -> new MovimientoResponse(
                        h.getEstadoOrigen() == null ? null : h.getEstadoOrigen().getNombre(),
                        h.getEstadoDestino().getNombre(), h.getUsuario().getNombres(), h.getRolUsuario(),
                        h.getNotaTecnica(), h.getFechaRegistro()))
                .toList();
        AccionResponse accion = TransicionSolicitud.siguiente(usuario.rol(), s.getEstado().getNombre())
                .map(paso -> new AccionResponse(paso.destino(), paso.requiereTecnico()))
                .orElse(null);
        return new SolicitudDetalleResponse(s.getId(), s.getCodigo(), s.getAsunto(), s.getDescripcion(),
                s.getCategoria().getNombre(), s.getArea().getNombre(), s.getPrioridad().getNombre(),
                s.getEstado().getNombre(), s.getSolicitante().getNombres(),
                s.getTecnico() == null ? null : s.getTecnico().getNombres(), s.getPabellon(), s.getNivel(),
                s.getAmbiente(), s.getFechaCreacion(), s.getFechaVencimiento(), adjuntos, movimientos, accion);
    }

    private List<SolicitudResumenResponse> resumir(List<Solicitud> lista) {
        if (lista.isEmpty()) {
            return List.of();
        }
        Map<Long, LocalDateTime> ultimos = historial.ultimosMovimientos(lista.stream().map(Solicitud::getId).toList())
                .stream().collect(Collectors.toMap(UltimoMovimiento::getSolicitudId, UltimoMovimiento::getFecha));
        return lista.stream().map(s -> SolicitudResumenResponse.from(s, ultimos.get(s.getId()))).toList();
    }

    public ArchivoEvidencia evidencia(UsuarioAutenticado usuario, String codigo, Long evidenciaId) {
        Solicitud s = visible(usuario, codigo);
        Evidencia e = evidencias.findById(evidenciaId)
                .filter(ev -> ev.getSolicitud() != null && Objects.equals(ev.getSolicitud().getId(), s.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("La evidencia no existe."));
        Path ruta = storage.ubicar(e.getUri())
                .orElseThrow(() -> new ResourceNotFoundException("El archivo de la evidencia ya no está disponible."));
        return new ArchivoEvidencia(ruta, e.getNombreArchivo(), e.getTipoMime());
    }

    /** Las solicitudes ajenas responden 404, igual que las inexistentes, para no revelar códigos válidos. */
    private Solicitud visible(UsuarioAutenticado usuario, String codigo) {
        return solicitudes.findByCodigo(codigo)
                .filter(s -> SolicitudAcceso.puedeVer(usuario, s))
                .orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADA));
    }
}
