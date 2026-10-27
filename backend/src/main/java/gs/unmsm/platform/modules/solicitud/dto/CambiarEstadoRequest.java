package gs.unmsm.platform.modules.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarEstadoRequest(
        @NotBlank(message = "Selecciona el estado destino.")
        String estadoDestino,
        @Size(max = 1000, message = "La nota debe tener máximo 1000 caracteres.")
        String notaTecnica
) {
}
