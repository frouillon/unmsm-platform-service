package gs.unmsm.platform.modules.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Reporte de solicitudes registradas en un rango de fechas (HU-24).")
public record ReporteSolicitudesResponse(
        @Schema(example = "2026-11-01") LocalDate desde,
        @Schema(example = "2026-11-30") LocalDate hasta,
        @Schema(description = "Área del reporte o «Todas las áreas».", example = "Mantenimiento general") String alcance,
        @Schema(example = "2026-11-26T15:00:00") LocalDateTime generadoEn,
        ResumenReporte resumen,
        List<CargaTecnico> cargaTecnica,
        List<FilaReporte> filas
) {
}
