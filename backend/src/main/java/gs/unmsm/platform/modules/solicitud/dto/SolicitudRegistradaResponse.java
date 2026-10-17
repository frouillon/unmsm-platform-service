package gs.unmsm.platform.modules.solicitud.dto;

import java.time.LocalDateTime;

public record SolicitudRegistradaResponse(
        Long id,
        String codigo,
        String asunto,
        String categoria,
        String area,
        String prioridad,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaVencimiento,
        int evidencias
) {
}
