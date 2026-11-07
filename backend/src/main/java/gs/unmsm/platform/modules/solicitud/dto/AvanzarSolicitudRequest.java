package gs.unmsm.platform.modules.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AvanzarSolicitudRequest(
        @NotBlank(message = "Indica el estado de destino.")
        String estado,
        Long tecnicoId,
        @Size(max = 500, message = "La nota admite hasta 500 caracteres.")
        String nota
) {
    public AvanzarSolicitudRequest {
        if (estado != null) {
            estado = estado.trim();
        }
        if (nota != null) {
            nota = nota.trim();
            if (nota.isEmpty()) {
                nota = null;
            }
        }
    }
}
