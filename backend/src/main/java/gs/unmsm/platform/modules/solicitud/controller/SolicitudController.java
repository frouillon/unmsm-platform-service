package gs.unmsm.platform.modules.solicitud.controller;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.dto.AvanzarSolicitudRequest;
import gs.unmsm.platform.modules.solicitud.dto.RegistrarSolicitudRequest;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudDetalleResponse;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudRegistradaResponse;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudResumenResponse;
import gs.unmsm.platform.modules.solicitud.dto.TecnicoResponse;
import gs.unmsm.platform.modules.solicitud.service.SolicitudConsultaService;
import gs.unmsm.platform.modules.solicitud.service.SolicitudFlujoService;
import gs.unmsm.platform.modules.solicitud.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final SolicitudConsultaService consultaService;
    private final SolicitudFlujoService flujoService;

    /** Multipart: parte "solicitud" (JSON) y, opcionalmente, partes "evidencias" (PNG, JPG o PDF). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<SolicitudRegistradaResponse> registrar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestPart("solicitud") RegistrarSolicitudRequest solicitud,
            @RequestPart(value = "evidencias", required = false) List<MultipartFile> evidencias) {
        SolicitudRegistradaResponse creada = solicitudService.registrar(usuario, solicitud, evidencias);
        return ResponseEntity.created(URI.create("/api/solicitudes/" + creada.codigo())).body(creada);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public List<SolicitudResumenResponse> mias(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaService.misSolicitudes(usuario);
    }

    @GetMapping("/area")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<SolicitudResumenResponse> delArea(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaService.delArea(usuario);
    }

    @GetMapping("/asignadas")
    @PreAuthorize("hasRole('TECNICO')")
    public List<SolicitudResumenResponse> asignadas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaService.asignadas(usuario);
    }

    @GetMapping("/tecnicos")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<TecnicoResponse> tecnicos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaService.tecnicosDelArea(usuario);
    }

    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<SolicitudResumenResponse> todas() {
        return consultaService.todas();
    }

    @GetMapping("/{codigo}/tecnicos")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMINISTRADOR')")
    public List<TecnicoResponse> tecnicosDeSolicitud(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                     @PathVariable String codigo) {
        return consultaService.tecnicosDeSolicitud(usuario, codigo);
    }

    @PostMapping("/{codigo}/estado")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'TECNICO', 'SUPERVISOR', 'ADMINISTRADOR')")
    public SolicitudDetalleResponse avanzar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                            @PathVariable String codigo,
                                            @Valid @RequestBody AvanzarSolicitudRequest request) {
        flujoService.avanzar(usuario, codigo, request);
        return consultaService.detalle(usuario, codigo);
    }

    @GetMapping("/{codigo}")
    public SolicitudDetalleResponse detalle(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                            @PathVariable String codigo) {
        return consultaService.detalle(usuario, codigo);
    }

    @GetMapping("/{codigo}/evidencias/{evidenciaId}")
    public ResponseEntity<Resource> evidencia(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                              @PathVariable String codigo, @PathVariable Long evidenciaId) {
        SolicitudConsultaService.ArchivoEvidencia archivo = consultaService.evidencia(usuario, codigo, evidenciaId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(new FileSystemResource(archivo.ruta()));
    }
}
