package gs.unmsm.platform.modules.solicitud.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;

import java.time.LocalDateTime;

public record SolicitudGestionResumenResponse(
        Long id,
        String codigo,
        String asunto,
        String categoria,
        String area,
        String prioridad,
        String estado,
        String solicitante,
        String tecnico,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fechaCreacion,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fechaVencimiento,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime ultimoMovimiento
) {
    public static SolicitudGestionResumenResponse from(Solicitud s, LocalDateTime ultimoMovimiento) {
        return new SolicitudGestionResumenResponse(
                s.getId(),
                s.getCodigo(),
                s.getAsunto(),
                s.getCategoria().getNombre(),
                s.getArea().getNombre(),
                s.getPrioridad().getNombre(),
                s.getEstado().getNombre(),
                s.getSolicitante().getNombres(),
                s.getTecnico() == null ? null : s.getTecnico().getNombres(),
                s.getFechaCreacion(),
                s.getFechaVencimiento(),
                ultimoMovimiento
        );
    }
}
