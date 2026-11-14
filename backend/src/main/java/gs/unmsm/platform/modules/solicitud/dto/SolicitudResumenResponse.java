package gs.unmsm.platform.modules.solicitud.dto;

import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resumen de una solicitud para listados.")
public record SolicitudResumenResponse(
        @Schema(description = "Identificador interno.", example = "15")
        Long id,

        @Schema(description = "Código único de la solicitud.", example = "SOL-2026-0001")
        String codigo,

        @Schema(description = "Asunto de la solicitud.", example = "Computadora del laboratorio no enciende")
        String asunto,

        @Schema(description = "Categoría.", example = "Soporte informático")
        String categoria,

        @Schema(description = "Área responsable.", example = "Tecnologías de la información")
        String area,

        @Schema(description = "Prioridad.", example = "Media")
        String prioridad,

        @Schema(description = "Estado actual.", example = "Pendiente")
        String estado,

        @Schema(description = "Técnico asignado; null si aún no tiene.", example = "Carlos Quispe")
        String tecnico,

        @Schema(description = "Fecha de creación.", example = "2026-11-14T09:30:00")
        LocalDateTime fechaCreacion,

        @Schema(description = "Fecha límite de atención según el SLA.", example = "2026-11-21T09:30:00")
        LocalDateTime fechaVencimiento,

        @Schema(description = "Última actualización registrada.", example = "2026-11-14T10:00:00")
        LocalDateTime ultimaActualizacion
) {
    public static SolicitudResumenResponse from(Solicitud s, LocalDateTime ultimaActualizacion) {
        return new SolicitudResumenResponse(s.getId(), s.getCodigo(), s.getAsunto(), s.getCategoria().getNombre(),
                s.getArea().getNombre(), s.getPrioridad().getNombre(), s.getEstado().getNombre(),
                s.getTecnico() == null ? null : s.getTecnico().getNombres(), s.getFechaCreacion(),
                s.getFechaVencimiento(), ultimaActualizacion == null ? s.getFechaCreacion() : ultimaActualizacion);
    }
}
