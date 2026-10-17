package gs.unmsm.platform.modules.solicitud.dto;

import gs.unmsm.platform.modules.solicitud.model.Solicitud;

import java.time.LocalDateTime;

public record SolicitudResumenResponse(
        Long id,
        String codigo,
        String asunto,
        String categoria,
        String area,
        String prioridad,
        String estado,
        String tecnico,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaVencimiento,
        LocalDateTime ultimaActualizacion
) {
    public static SolicitudResumenResponse from(Solicitud s, LocalDateTime ultimaActualizacion) {
        return new SolicitudResumenResponse(s.getId(), s.getCodigo(), s.getAsunto(), s.getCategoria().getNombre(),
                s.getArea().getNombre(), s.getPrioridad().getNombre(), s.getEstado().getNombre(),
                s.getTecnico() == null ? null : s.getTecnico().getNombres(), s.getFechaCreacion(),
                s.getFechaVencimiento(), ultimaActualizacion == null ? s.getFechaCreacion() : ultimaActualizacion);
    }
}
