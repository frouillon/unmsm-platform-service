package gs.unmsm.platform.modules.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Área seleccionable en el filtro del reporte.")
public record AreaOpcion(@Schema(example = "1") Long id, @Schema(example = "Mantenimiento general") String nombre) {
}
