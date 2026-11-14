package gs.unmsm.platform.modules.solicitud.dto;

import gs.unmsm.platform.modules.solicitud.model.Prioridad;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Prioridad disponible para registrar solicitudes.")
public record PrioridadResponse(
        @Schema(description = "Identificador de la prioridad.", example = "2")
        Long id,

        @Schema(description = "Nombre de la prioridad.", example = "Media")
        String nombre,

        @Schema(description = "Días de plazo máximo de atención según el SLA.", example = "7")
        int slaDias,

        @Schema(description = "Indica si el plazo se cuenta en días hábiles.", example = "true")
        boolean diasHabiles
) {

    public static PrioridadResponse from(Prioridad prioridad) {
        return new PrioridadResponse(prioridad.getId(), prioridad.getNombre(), prioridad.getSlaDias(),
                Boolean.TRUE.equals(prioridad.getDiasHabiles()));
    }
}
