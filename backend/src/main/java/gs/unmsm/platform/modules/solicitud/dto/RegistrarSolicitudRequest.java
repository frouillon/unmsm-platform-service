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

        @NotBlank(message = "Indica el pabellón.")
        @Size(max = 100, message = "El pabellón admite hasta 100 caracteres.")
        String pabellon,

        @NotBlank(message = "Indica el piso o nivel.")
        @Size(max = 50, message = "El piso o nivel admite hasta 50 caracteres.")
        String nivel,

        @NotBlank(message = "Indica el aula o ambiente.")
        @Size(max = 100, message = "El aula o ambiente admite hasta 100 caracteres.")
        String ambiente
) {
    public RegistrarSolicitudRequest {
        asunto = strip(asunto);
        descripcion = strip(descripcion);
        pabellon = strip(pabellon);
        nivel = strip(nivel);
        ambiente = strip(ambiente);
    }

    private static String strip(String valor) {
        return valor == null ? null : valor.strip();
    }
}
