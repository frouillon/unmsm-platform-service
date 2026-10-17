package gs.unmsm.platform.modules.solicitud.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitudDetalleResponse(
        Long id,
        String codigo,
        String asunto,
        String descripcion,
        String categoria,
        String area,
        String prioridad,
        String estado,
        String solicitante,
        String tecnico,
        String pabellon,
        String nivel,
        String ambiente,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaVencimiento,
        List<EvidenciaResponse> evidencias,
        List<MovimientoResponse> historial
) {
    public record EvidenciaResponse(Long id, String nombre, String tipoMime, long tamanoBytes) {
    }

    public record MovimientoResponse(
            String estadoOrigen,
            String estadoDestino,
            String usuario,
            String rol,
            String nota,
            LocalDateTime fecha
    ) {
    }
}
