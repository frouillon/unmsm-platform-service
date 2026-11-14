package gs.unmsm.platform.modules.solicitud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para cambiar el estado de una solicitud.")
public record AvanzarSolicitudRequest(
        @Schema(description = "Estado de destino permitido según el rol y el estado actual.",
                example = "Asignado", requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"En revisión", "Asignado", "En proceso", "Resuelto", "Cerrado"})
        @NotBlank(message = "Indica el estado de destino.")
        String estado,

        @Schema(description = "Identificador del técnico. Obligatorio al pasar de En revisión a Asignado.",
                example = "6")
        Long tecnicoId,

        @Schema(description = "Nota opcional que queda registrada en el historial.",
                example = "Se asigna al técnico de turno.", maxLength = 500)
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
