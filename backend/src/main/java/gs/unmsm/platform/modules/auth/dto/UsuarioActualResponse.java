package gs.unmsm.platform.modules.auth.dto;

import gs.unmsm.platform.modules.usuario.model.Area;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos del usuario autenticado.")
public record UsuarioActualResponse(
        @Schema(description = "Identificador del usuario.", example = "1")
        Long id,

        @Schema(description = "Nombres y apellidos.", example = "Andrea Mendoza")
        String nombres,

        @Schema(description = "Correo institucional.", example = "andrea.mendoza@unmsm.edu.pe")
        String correo,

        @Schema(description = "Rol del usuario.", example = "ESTUDIANTE",
                allowableValues = {"ESTUDIANTE", "TECNICO", "SUPERVISOR", "ADMINISTRADOR"})
        String rol,

        @Schema(description = "Identificador del área responsable; null si el rol no tiene área.", example = "1")
        Long areaId,

        @Schema(description = "Nombre del área responsable; null si el rol no tiene área.",
                example = "Tecnologías de la información")
        String areaNombre
) {

    public static UsuarioActualResponse from(Usuario usuario) {
        Area area = usuario.getArea();
        return new UsuarioActualResponse(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getCorreoInstitucional(),
                usuario.getRol().getNombre(),
                area == null ? null : area.getId(),
                area == null ? null : area.getNombre());
    }
}
