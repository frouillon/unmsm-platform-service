package gs.unmsm.platform.modules.solicitud.controller;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.dto.AvanzarSolicitudRequest;
import gs.unmsm.platform.modules.solicitud.dto.CrearComentarioRequest;
import gs.unmsm.platform.modules.solicitud.dto.RegistrarSolicitudRequest;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudDetalleResponse;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudRegistradaResponse;
import gs.unmsm.platform.modules.solicitud.dto.SolicitudResumenResponse;
import gs.unmsm.platform.modules.solicitud.dto.TecnicoResponse;
import gs.unmsm.platform.modules.solicitud.service.ComentarioService;
import gs.unmsm.platform.modules.solicitud.service.SolicitudConsultaService;
import gs.unmsm.platform.modules.solicitud.service.SolicitudFlujoService;
import gs.unmsm.platform.modules.solicitud.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Solicitudes", description = "Registro, consulta, evidencias y flujo de estados de las solicitudes.")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final SolicitudConsultaService consultaService;
    private final ComentarioService comentarioService;
    private final SolicitudFlujoService flujoService;

    /** Multipart: parte "solicitud" (JSON) y, opcionalmente, partes "evidencias" (PNG, JPG o PDF). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @Operation(
            summary = "Registrar solicitud",
            description = """
                    Solo **ESTUDIANTE**. Envía una petición `multipart/form-data` con dos partes:
                    - `solicitud`: JSON con categoría, prioridad, asunto, descripción y ubicación.
                    - `evidencias`: opcional, hasta 5 archivos PNG, JPG o PDF de 5 MB cada uno.

                    La ubicación es obligatoria cuando la categoría la exige. El sistema asigna el código,
                    el estado inicial `Pendiente` y la fecha de vencimiento según el SLA de la prioridad.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud registrada; devuelve su código y datos."),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "413", ref = "#/components/responses/PayloadTooLarge")
    })
    public ResponseEntity<SolicitudRegistradaResponse> registrar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestPart("solicitud") RegistrarSolicitudRequest solicitud,
            @RequestPart(value = "evidencias", required = false) List<MultipartFile> evidencias) {
        SolicitudRegistradaResponse creada = solicitudService.registrar(usuario, solicitud, evidencias);
        return ResponseEntity.created(URI.create("/api/solicitudes/" + creada.codigo())).body(creada);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @Operation(summary = "Listar mis solicitudes", description = "Solo **ESTUDIANTE**. Devuelve las solicitudes registradas por el usuario del token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitudes del estudiante, más recientes primero."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public List<SolicitudResumenResponse> mias(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaService.misSolicitudes(usuario);
    }

    @GetMapping("/area")
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Operation(summary = "Listar solicitudes del área", description = "Solo **SUPERVISOR**. Devuelve las solicitudes del área a la que pertenece el usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitudes del área del supervisor."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public List<SolicitudResumenResponse> delArea(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaService.delArea(usuario);
    }

    @GetMapping("/asignadas")
    @PreAuthorize("hasRole('TECNICO')")
    @Operation(summary = "Listar solicitudes asignadas", description = "Solo **TECNICO**. Devuelve las solicitudes asignadas al técnico del token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitudes asignadas al técnico."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public List<SolicitudResumenResponse> asignadas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaService.asignadas(usuario);
    }

    @GetMapping("/tecnicos")
    @PreAuthorize("hasRole('SUPERVISOR')")
    @Operation(summary = "Listar técnicos del área", description = "Solo **SUPERVISOR**. Devuelve los técnicos activos del área para poder asignar una solicitud.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnicos activos del área."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public List<TecnicoResponse> tecnicos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return consultaService.tecnicosDelArea(usuario);
    }

    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar todas las solicitudes", description = "Solo **ADMINISTRADOR**. Devuelve las solicitudes de todas las áreas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todas las solicitudes registradas."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public List<SolicitudResumenResponse> todas() {
        return consultaService.todas();
    }

    @GetMapping("/{codigo}/tecnicos")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar técnicos candidatos para una solicitud",
            description = "Solo **SUPERVISOR** o **ADMINISTRADOR**. Devuelve los técnicos activos del área de la solicitud.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnicos disponibles para asignar."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public List<TecnicoResponse> tecnicosDeSolicitud(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Código único de la solicitud.", example = "SOL-2026-0001")
            @PathVariable String codigo) {
        return consultaService.tecnicosDeSolicitud(usuario, codigo);
    }

    @PostMapping("/{codigo}/estado")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'TECNICO', 'SUPERVISOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Avanzar el estado de una solicitud",
            description = """
                    Cambia la solicitud al siguiente estado permitido según el rol y el estado actual:

                    | Rol | Transición permitida |
                    |---|---|
                    | SUPERVISOR / ADMINISTRADOR | Pendiente → En revisión → Asignado (requiere `tecnicoId`); En proceso → Resuelto (requiere nota); Resuelto → Cerrado |
                    | TECNICO | Asignado → En proceso → Resuelto (requiere nota) |
                    | ESTUDIANTE | Resuelto → Cerrado |

                    Al pasar a **Resuelto** la nota técnica es obligatoria. En los demás pasos es opcional.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud actualizada; devuelve el detalle completo."),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    public SolicitudDetalleResponse avanzar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Código único de la solicitud.", example = "SOL-2026-0001")
            @PathVariable String codigo,
            @Valid @RequestBody AvanzarSolicitudRequest request) {
        flujoService.avanzar(usuario, codigo, request);
        return consultaService.detalle(usuario, codigo);
    }

    @PostMapping("/{codigo}/comentarios")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'TECNICO', 'SUPERVISOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Publicar un comentario",
            description = "Cualquier usuario con acceso a la solicitud puede comentar, excepto si ya está **Cerrada**. "
                    + "Devuelve el detalle actualizado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentario publicado; devuelve el detalle completo."),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    public SolicitudDetalleResponse comentar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Código único de la solicitud.", example = "SOL-2026-0001")
            @PathVariable String codigo,
            @Valid @RequestBody CrearComentarioRequest request) {
        comentarioService.comentar(usuario, codigo, request);
        return consultaService.detalle(usuario, codigo);
    }

    @GetMapping("/{codigo}")
    @Operation(
            summary = "Obtener el detalle de una solicitud",
            description = "Devuelve la solicitud con evidencias, comentarios, historial y la siguiente acción disponible. "
                    + "El acceso depende del rol: estudiante (propia), técnico (asignada), supervisor (su área) y administrador (todas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de la solicitud."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public SolicitudDetalleResponse detalle(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Código único de la solicitud.", example = "SOL-2026-0001")
            @PathVariable String codigo) {
        return consultaService.detalle(usuario, codigo);
    }

    @GetMapping("/{codigo}/evidencias/{evidenciaId}")
    @Operation(
            summary = "Descargar una evidencia",
            description = "Devuelve el archivo (PNG, JPG o PDF) asociado a la solicitud. "
                    + "Se aplican las mismas reglas de acceso que al detalle de la solicitud.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo binario de la evidencia.",
                    content = @Content(mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public ResponseEntity<Resource> evidencia(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Código único de la solicitud.", example = "SOL-2026-0001")
            @PathVariable String codigo,
            @Parameter(description = "Identificador de la evidencia.", example = "1")
            @PathVariable Long evidenciaId) {
        SolicitudConsultaService.ArchivoEvidencia archivo = consultaService.evidencia(usuario, codigo, evidenciaId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(new FileSystemResource(archivo.ruta()));
    }
}
