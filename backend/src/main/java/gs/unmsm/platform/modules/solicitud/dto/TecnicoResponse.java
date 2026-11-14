package gs.unmsm.platform.modules.solicitud.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Técnico activo disponible para asignar una solicitud.")
public record TecnicoResponse(
        @Schema(description = "Identificador del técnico.", example = "6")
        Long id,

        @Schema(description = "Nombres y apellidos.", example = "Carlos Quispe")
        String nombres
) {
}
