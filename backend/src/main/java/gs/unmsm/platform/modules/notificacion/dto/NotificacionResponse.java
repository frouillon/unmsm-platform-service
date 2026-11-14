package gs.unmsm.platform.modules.notificacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Aviso in-app vinculado a una solicitud.")
public record NotificacionResponse(
        @Schema(description = "Identificador.", example = "1")
        Long id,

        @Schema(description = "Tipo de aviso.", example = "CAMBIO_ESTADO")
        String tipo,

        @Schema(description = "Texto del aviso.", example = "Tu solicitud SOL-2026-0001 pasó a En revisión.")
        String mensaje,

        @Schema(description = "Código de la solicitud relacionada.", example = "SOL-2026-0001")
        String codigoSolicitud,

        @Schema(description = "Indica si ya fue leída.", example = "false")
        boolean leida,

        @Schema(description = "Fecha de creación.", example = "2026-11-14T12:00:00")
        LocalDateTime fecha
) {
}
