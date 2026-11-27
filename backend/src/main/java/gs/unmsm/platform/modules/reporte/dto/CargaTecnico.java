package gs.unmsm.platform.modules.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fila de la matriz de carga técnica (HU-23).")
public record CargaTecnico(
        @Schema(example = "Diana Cárdenas") String tecnico,
        @Schema(description = "Solicitudes en estado Asignado.", example = "2") long asignadas,
        @Schema(description = "Solicitudes en estado En proceso.", example = "1") long enProceso,
        @Schema(description = "Solicitudes resueltas o cerradas.", example = "5") long resueltas
) {
}
