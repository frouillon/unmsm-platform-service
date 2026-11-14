package gs.unmsm.platform.modules.solicitud.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detalle completo de una solicitud, con evidencias, historial y siguiente acción.")
public record SolicitudDetalleResponse(
        @Schema(description = "Identificador interno.", example = "15")
        Long id,

        @Schema(description = "Código único de la solicitud.", example = "SOL-2026-0001")
        String codigo,

        @Schema(description = "Asunto de la solicitud.", example = "Computadora del laboratorio no enciende")
        String asunto,

        @Schema(description = "Descripción del problema.",
                example = "La computadora 12 del laboratorio 204 no enciende desde esta mañana.")
        String descripcion,

        @Schema(description = "Categoría.", example = "Soporte informático")
        String categoria,

        @Schema(description = "Área responsable.", example = "Tecnologías de la información")
        String area,

        @Schema(description = "Prioridad.", example = "Media")
        String prioridad,

        @Schema(description = "Estado actual.", example = "Asignado")
        String estado,

        @Schema(description = "Nombre del solicitante.", example = "Andrea Mendoza")
        String solicitante,

        @Schema(description = "Nombre del técnico asignado; null si aún no tiene.", example = "Carlos Quispe")
        String tecnico,

        @Schema(description = "Pabellón o edificio.", example = "Pabellón de Ingeniería de Sistemas")
        String pabellon,

        @Schema(description = "Piso o nivel.", example = "2")
        String nivel,

        @Schema(description = "Aula o ambiente.", example = "Laboratorio 204")
        String ambiente,

        @Schema(description = "Fecha de creación.", example = "2026-11-14T09:30:00")
        LocalDateTime fechaCreacion,

        @Schema(description = "Fecha límite de atención según el SLA.", example = "2026-11-21T09:30:00")
        LocalDateTime fechaVencimiento,

        @Schema(description = "Evidencias adjuntas.")
        List<EvidenciaResponse> evidencias,

        @Schema(description = "Historial de cambios de estado.")
        List<MovimientoResponse> historial,

        @Schema(description = "Siguiente acción que puede ejecutar el usuario; null si no le corresponde moverla.")
        AccionResponse accion
) {
    @Schema(description = "Siguiente transición disponible para el usuario autenticado.")
    public record AccionResponse(
            @Schema(description = "Estado de destino.", example = "En proceso")
            String estado,

            @Schema(description = "Indica si la transición exige elegir un técnico.", example = "false")
            boolean requiereTecnico
    ) {
    }

    @Schema(description = "Evidencia adjunta a la solicitud.")
    public record EvidenciaResponse(
            @Schema(description = "Identificador de la evidencia.", example = "1")
            Long id,

            @Schema(description = "Nombre original del archivo.", example = "foto-equipo.jpg")
            String nombre,

            @Schema(description = "Tipo MIME del archivo.", example = "image/jpeg")
            String tipoMime,

            @Schema(description = "Tamaño en bytes.", example = "245760")
            long tamanoBytes
    ) {
    }

    @Schema(description = "Movimiento registrado en el historial de la solicitud.")
    public record MovimientoResponse(
            @Schema(description = "Estado anterior.", example = "Pendiente")
            String estadoOrigen,

            @Schema(description = "Estado nuevo.", example = "En revisión")
            String estadoDestino,

            @Schema(description = "Usuario que realizó el cambio.", example = "Lucía Ramos")
            String usuario,

            @Schema(description = "Rol del usuario que realizó el cambio.", example = "SUPERVISOR")
            String rol,

            @Schema(description = "Nota registrada, si existe.", example = "Se revisa el caso.")
            String nota,

            @Schema(description = "Fecha del movimiento.", example = "2026-11-14T10:00:00")
            LocalDateTime fecha
    ) {
    }
}
