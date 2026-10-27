package gs.unmsm.platform.modules.solicitud.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AsignarSolicitudRequest(
        @NotNull(message = "Selecciona un técnico.")
        Long tecnicoId,
        @Size(max = 1000, message = "La nota debe tener máximo 1000 caracteres.")
        String notaTecnica
) {
}
