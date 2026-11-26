package gs.unmsm.platform.modules.reporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Una solicitud dentro del reporte.")
public record FilaReporte(
        @Schema(example = "SOL-2026-0007") String codigo,
        @Schema(example = "2026-11-10T09:30:00") LocalDateTime fechaCreacion,
        @Schema(example = "Mantenimiento") String categoria,
        @Schema(example = "Mantenimiento general") String area,
        @Schema(example = "Alta") String prioridad,
        @Schema(example = "Resuelto") String estado,
        @Schema(description = "Técnico asignado; null si aún no tiene.", example = "Diana Cárdenas", nullable = true)
        String tecnico,
        @Schema(example = "2026-11-13T09:30:00") LocalDateTime fechaVencimiento,
        @Schema(description = "Primera vez que llegó a Resuelto; null si sigue abierta.", nullable = true)
        LocalDateTime fechaResolucion,
        @Schema(description = "Días (con un decimal) entre el registro y la resolución.", example = "1.5", nullable = true)
        BigDecimal diasAtencion,
        @Schema(description = "A tiempo, Fuera de plazo, En plazo o Vencida.", example = "A tiempo")
        String cumplimiento
) {
    public static final String A_TIEMPO = "A tiempo";
    public static final String FUERA_DE_PLAZO = "Fuera de plazo";
    public static final String EN_PLAZO = "En plazo";
    public static final String VENCIDA = "Vencida";
}
