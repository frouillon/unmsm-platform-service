package gs.unmsm.platform.modules.sla.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado del envío del consolidado diario.")
public record ResultadoConsolidado(
        @Schema(description = "Supervisores que recibieron el resumen en esta ejecución.", example = "2")
        int enviados
) {
}
