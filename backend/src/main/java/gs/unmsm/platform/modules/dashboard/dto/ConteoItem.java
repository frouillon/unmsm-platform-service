package gs.unmsm.platform.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cantidad agrupada por un nombre de catálogo.")
public record ConteoItem(
        @Schema(description = "Nombre del valor (estado, área o prioridad).", example = "Pendiente")
        String nombre,

        @Schema(description = "Cantidad de solicitudes.", example = "4")
        long cantidad
) {
}
