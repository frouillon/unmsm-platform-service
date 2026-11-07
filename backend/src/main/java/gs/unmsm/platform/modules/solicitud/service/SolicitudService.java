package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.exception.FieldValidationException;
import gs.unmsm.platform.common.exception.UnauthorizedException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.dto.RegistrarSolicitudRequest;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudRegistradaResponse;
import gs.unmsm.platform.modules.solicitud.model.Categoria;
import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Evidencia;
import gs.unmsm.platform.modules.solicitud.model.Prioridad;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.CategoriaRepository;
import gs.unmsm.platform.modules.solicitud.repository.EstadoRepository;
import gs.unmsm.platform.modules.solicitud.repository.EvidenciaRepository;
import gs.unmsm.platform.modules.solicitud.repository.PrioridadRepository;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.model.HistorialEstado;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudes;
    private final CategoriaRepository categorias;
    private final PrioridadRepository prioridades;
    private final EstadoRepository estados;
    private final UsuarioRepository usuarios;
    private final HistorialEstadoRepository historial;
    private final EvidenciaRepository evidencias;
    private final CodigoSolicitudGenerator codigos;
    private final EvidenciaStorage storage;
    private final Clock clock;

    @Transactional
    public SolicitudRegistradaResponse registrar(UsuarioAutenticado autenticado, RegistrarSolicitudRequest request,
                                                 List<MultipartFile> archivos) {
        Usuario solicitante = usuarios.findById(autenticado.id())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new UnauthorizedException("Tu sesión ya no es válida. Inicia sesión nuevamente."));

        Categoria categoria = categorias.findById(request.categoriaId())
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new FieldValidationException("categoriaId", "La categoría seleccionada no está disponible."));
        Prioridad prioridad = prioridades.findById(request.prioridadId())
                .orElseThrow(() -> new FieldValidationException("prioridadId", "La prioridad seleccionada no existe."));
        List<EvidenciaStorage.ArchivoValidado> adjuntos = storage.validar(archivos);

        Estado pendiente = estados.findByNombre(EstadoNombre.PENDIENTE)
                .orElseThrow(() -> new IllegalStateException("Falta el estado inicial " + EstadoNombre.PENDIENTE));

        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        Solicitud solicitud = solicitudes.save(Solicitud.builder()
                .codigo(codigos.siguiente(ahora.getYear()))
                .solicitante(solicitante)
                .area(categoria.getArea())
                .categoria(categoria)
                .prioridad(prioridad)
                .estado(pendiente)
                .asunto(request.asunto())
                .descripcion(request.descripcion())
                .pabellon(request.pabellon())
                .nivel(request.nivel())
                .ambiente(request.ambiente())
                .fechaCreacion(ahora)
                .fechaVencimiento(SlaCalculator.vencimiento(ahora, prioridad))
                .build());

        historial.save(HistorialEstado.builder()
                .solicitud(solicitud)
                .estadoDestino(pendiente)
                .usuario(solicitante)
                .rolUsuario(autenticado.rol())
                .fechaRegistro(ahora)
                .build());

        List<Evidencia> guardadas = storage.guardar(solicitud.getCodigo(), adjuntos).stream()
                .map(a -> Evidencia.builder()
                        .solicitud(solicitud)
                        .nombreArchivo(a.nombreOriginal())
                        .uri(a.uri())
                        .tipoMime(a.tipoMime())
                        .tamanoBytes(a.tamanoBytes())
                        .fechaCarga(ahora)
                        .build())
                .toList();
        evidencias.saveAll(guardadas);

        return new SolicitudRegistradaResponse(solicitud.getId(), solicitud.getCodigo(), solicitud.getAsunto(),
                categoria.getNombre(), categoria.getArea().getNombre(), prioridad.getNombre(), pendiente.getNombre(),
                solicitud.getFechaCreacion(), solicitud.getFechaVencimiento(), guardadas.size());
    }
}
