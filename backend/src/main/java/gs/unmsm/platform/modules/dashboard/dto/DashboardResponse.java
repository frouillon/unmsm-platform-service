package gs.unmsm.platform.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Indicadores del alcance del usuario autenticado (RF-39/RF-40).")
public record DashboardResponse(
        @Schema(description = "Total de solicitudes en el alcance.", example = "12")
        long total,

        @Schema(description = "Solicitudes activas con plazo vencido.", example = "2")
        long vencidas,

        @Schema(description = "Distribución por estado.")
        List<ConteoItem> porEstado,

        @Schema(description = "Distribución por área.")
        List<ConteoItem> porArea,

        @Schema(description = "Distribución por prioridad.")
        List<ConteoItem> porPrioridad
) {
}
