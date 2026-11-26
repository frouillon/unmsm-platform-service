package gs.unmsm.platform.modules.sla.controller;

import gs.unmsm.platform.modules.sla.dto.ResultadoConsolidado;
import gs.unmsm.platform.modules.sla.dto.ResultadoEvaluacionSla;
import gs.unmsm.platform.modules.sla.service.SlaAlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ejecución manual de los procesos programados, útil para soporte y para demostraciones. */
@RestController
@RequestMapping("/api/admin/sla")
@Tag(name = "Alertas de SLA", description = "Procesos programados de vencimiento y consolidado diario.")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class SlaAlertaController {

    private final SlaAlertaService servicio;

    @PostMapping("/evaluar")
    @Operation(
            summary = "Evaluar plazos ahora",
            description = "Solo **ADMINISTRADOR**. Ejecuta de inmediato la revisión de solicitudes en riesgo o vencidas "
                    + "que normalmente corre cada 15 minutos. No repite avisos ya enviados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen de la evaluación."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public ResultadoEvaluacionSla evaluar() {
        return servicio.evaluar();
    }

    @PostMapping("/consolidado")
    @Operation(
            summary = "Enviar el consolidado diario ahora",
            description = "Solo **ADMINISTRADOR**. Envía el resumen del área a cada supervisor que aún no lo recibió hoy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cantidad de supervisores avisados."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    public ResultadoConsolidado consolidado() {
        return servicio.enviarConsolidado();
    }
}
