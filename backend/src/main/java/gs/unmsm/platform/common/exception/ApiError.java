package gs.unmsm.platform.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Formato estándar de error de la API.")
public record ApiError(
        @Schema(description = "Fecha y hora del error.", example = "2026-11-14T10:00:00")
        LocalDateTime timestamp,

        @Schema(description = "Código HTTP.", example = "400")
        int status,

        @Schema(description = "Descripción corta del estado HTTP.", example = "Bad Request")
        String error,

        @Schema(description = "Mensaje legible para el usuario.", example = "Hay campos con errores. Revisa el formulario.")
        String message,

        @Schema(description = "Ruta del endpoint que produjo el error.", example = "/api/solicitudes")
        String path,

        @Schema(description = "Errores por campo, cuando corresponde.",
                example = "{\"asunto\":\"Escribe el asunto de la solicitud.\"}")
        Map<String, String> fieldErrors
) {
}
