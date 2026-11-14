package gs.unmsm.platform.modules.solicitud.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta al registrar una solicitud.")
public record SolicitudRegistradaResponse(
        @Schema(description = "Identificador interno.", example = "15")
        Long id,

        @Schema(description = "Código generado automáticamente.", example = "SOL-2026-0001")
        String codigo,

        @Schema(description = "Asunto registrado.", example = "Computadora del laboratorio no enciende")
        String asunto,

        @Schema(description = "Categoría.", example = "Soporte informático")
        String categoria,

        @Schema(description = "Área responsable.", example = "Tecnologías de la información")
        String area,

        @Schema(description = "Prioridad.", example = "Media")
        String prioridad,

        @Schema(description = "Estado inicial.", example = "Pendiente")
        String estado,

        @Schema(description = "Fecha de creación.", example = "2026-11-14T09:30:00")
        LocalDateTime fechaCreacion,

        @Schema(description = "Fecha límite de atención según el SLA.", example = "2026-11-21T09:30:00")
        LocalDateTime fechaVencimiento,

        @Schema(description = "Cantidad de evidencias adjuntadas.", example = "2")
        int evidencias
) {
}
