package gs.unmsm.platform.modules.solicitud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Texto de un comentario sobre la solicitud.")
public record CrearComentarioRequest(
        @Schema(description = "Contenido del comentario.", example = "¿Ya revisaron el proyector del aula 204?")
        @NotBlank(message = "Escribe un comentario.")
        @Size(max = 2000, message = "El comentario admite hasta 2000 caracteres.")
        String contenido
) {
    public CrearComentarioRequest {
        if (contenido != null) {
            contenido = contenido.trim();
        }
    }
}
