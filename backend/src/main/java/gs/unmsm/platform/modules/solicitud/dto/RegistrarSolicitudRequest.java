package gs.unmsm.platform.modules.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarSolicitudRequest(
        @NotNull(message = "Selecciona una categoría.")
        Long categoriaId,

        @NotNull(message = "Selecciona una prioridad.")
        Long prioridadId,

        @NotBlank(message = "Escribe el asunto de la solicitud.")
        @Size(max = 100, message = "El asunto admite hasta 100 caracteres.")
        String asunto,

        @NotBlank(message = "Describe qué ocurre.")
        @Size(min = 20, max = 2000, message = "La descripción debe tener entre 20 y 2000 caracteres.")
        String descripcion,

        /* La ubicación es obligatoria solo si la categoría lo exige; lo valida SolicitudService. */
        @Size(max = 100, message = "El pabellón admite hasta 100 caracteres.")
        String pabellon,

        @Size(max = 50, message = "El piso o nivel admite hasta 50 caracteres.")
        String nivel,

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
