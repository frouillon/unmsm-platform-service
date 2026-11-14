package gs.unmsm.platform.modules.solicitud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar una solicitud. Se envía como la parte `solicitud` del multipart.")
public record RegistrarSolicitudRequest(
        @Schema(description = "Identificador de la categoría de la solicitud.", example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Selecciona una categoría.")
        Long categoriaId,

        @Schema(description = "Identificador de la prioridad de la solicitud.", example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Selecciona una prioridad.")
        Long prioridadId,

        @Schema(description = "Asunto breve de la solicitud.", example = "Computadora del laboratorio no enciende",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
        @NotBlank(message = "Escribe el asunto de la solicitud.")
        @Size(max = 100, message = "El asunto admite hasta 100 caracteres.")
        String asunto,

        @Schema(description = "Descripción detallada del problema.",
                example = "La computadora 12 del laboratorio 204 no enciende desde esta mañana.",
                requiredMode = Schema.RequiredMode.REQUIRED, minLength = 20, maxLength = 2000)
        @NotBlank(message = "Describe qué ocurre.")
        @Size(min = 20, max = 2000, message = "La descripción debe tener entre 20 y 2000 caracteres.")
        String descripcion,

        /* La ubicación es obligatoria solo si la categoría lo exige; lo valida SolicitudService. */
        @Schema(description = "Pabellón o edificio. Obligatorio cuando la categoría exige ubicación.",
                example = "Pabellón de Ingeniería de Sistemas", maxLength = 100)
        @Size(max = 100, message = "El pabellón admite hasta 100 caracteres.")
        String pabellon,

        @Schema(description = "Piso o nivel. Obligatorio cuando la categoría exige ubicación.",
                example = "2", maxLength = 50)
        @Size(max = 50, message = "El piso o nivel admite hasta 50 caracteres.")
        String nivel,

        @Schema(description = "Aula o ambiente. Obligatorio cuando la categoría exige ubicación.",
                example = "Laboratorio 204", maxLength = 100)
        @Size(max = 100, message = "El aula o ambiente admite hasta 100 caracteres.")
        String ambiente
) {
    public RegistrarSolicitudRequest {
        asunto = strip(asunto);
        descripcion = strip(descripcion);
        pabellon = blankToNull(pabellon);
        nivel = blankToNull(nivel);
        ambiente = blankToNull(ambiente);
    }

    private static String strip(String valor) {
        return valor == null ? null : valor.strip();
    }

    private static String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
