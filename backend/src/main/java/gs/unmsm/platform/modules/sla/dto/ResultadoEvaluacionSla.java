package gs.unmsm.platform.modules.sla.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de una pasada del proceso de alertas de SLA.")
public record ResultadoEvaluacionSla(
        @Schema(description = "Solicitudes abiertas revisadas (en riesgo o vencidas).", example = "5")
        int revisadas,

        @Schema(description = "Solicitudes a las que les queda menos del umbral para vencer.", example = "3")
        int enRiesgo,

        @Schema(description = "Solicitudes abiertas con el plazo ya vencido.", example = "2")
        int vencidas,

        @Schema(description = "Notificaciones nuevas creadas en esta pasada (no se repiten).", example = "4")
        int alertasEmitidas
) {
}
