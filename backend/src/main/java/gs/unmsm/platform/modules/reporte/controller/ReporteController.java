package gs.unmsm.platform.modules.reporte.controller;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.reporte.dto.AreaOpcion;
import gs.unmsm.platform.modules.reporte.dto.ReporteSolicitudesResponse;
import gs.unmsm.platform.modules.reporte.service.ReporteCsv;
import gs.unmsm.platform.modules.reporte.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Reportes de solicitudes con filtros temporales, exportables a CSV.")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMINISTRADOR')")
public class ReporteController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final ReporteService reportes;

    @GetMapping("/solicitudes")
    @Operation(
            summary = "Reporte de solicitudes",
            description = """
                    **SUPERVISOR** (solo su área) o **ADMINISTRADOR** (todas, o una con `areaId`).
                    Incluye las solicitudes registradas entre `desde` y `hasta` (ambos inclusive, máximo un año;
                    por defecto los últimos 30 días), con cumplimiento de SLA, MTTR y matriz de carga técnica.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte generado."),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public ReporteSolicitudesResponse reporte(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Fecha inicial (inclusive), formato AAAA-MM-DD.", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final (inclusive), formato AAAA-MM-DD.", example = "2026-11-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @Parameter(description = "Solo administrador: limita el reporte a un área.", example = "1")
            @RequestParam(required = false) Long areaId) {
        return reportes.generar(usuario, desde, hasta, areaId);
    }

    @GetMapping("/solicitudes/csv")
    @Operation(summary = "Exportar el reporte a CSV", description = "Mismos filtros y permisos que el reporte en JSON.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo CSV (UTF-8 con BOM)."),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public ResponseEntity<byte[]> csv(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long areaId) {
        ReporteSolicitudesResponse reporte = reportes.generar(usuario, desde, hasta, areaId);
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(ReporteCsv.nombreArchivo(reporte), StandardCharsets.UTF_8).build().toString())
                .body(ReporteCsv.generar(reporte));
    }

    @GetMapping("/areas")
    @Operation(summary = "Áreas disponibles para el filtro",
            description = "El supervisor recibe solo su área; el administrador, todas las activas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Áreas."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public List<AreaOpcion> areas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reportes.areasDisponibles(usuario);
    }
}
