package gs.unmsm.platform.modules.reporte.dto;

import gs.unmsm.platform.modules.dashboard.dto.ConteoItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Indicadores del periodo del reporte.")
public record ResumenReporte(
        @Schema(description = "Solicitudes registradas en el periodo.", example = "24") long total,
        @Schema(description = "Resueltas o cerradas.", example = "15") long atendidas,
        @Schema(description = "Todavía abiertas.", example = "9") long abiertas,
        @Schema(description = "Abiertas con el plazo vencido.", example = "2") long vencidas,
        @Schema(description = "Atendidas después de su fecha límite.", example = "1") long atendidasFueraDePlazo,
        @Schema(description = "Porcentaje de atendidas dentro del plazo; null si aún no hay atendidas.",
                example = "93.3", nullable = true)
        BigDecimal cumplimientoSla,
        @Schema(description = "Tiempo medio de resolución (MTTR) en días; null si aún no hay atendidas.",
                example = "2.4", nullable = true)
        BigDecimal mttrDias,
        List<ConteoItem> porEstado,
        List<ConteoItem> porCategoria,
        List<ConteoItem> porPrioridad
) {
}
