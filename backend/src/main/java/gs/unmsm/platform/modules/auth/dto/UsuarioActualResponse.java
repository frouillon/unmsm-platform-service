package gs.unmsm.platform.modules.auth.dto;

import gs.unmsm.platform.modules.usuario.model.Area;
import gs.unmsm.platform.modules.usuario.model.Usuario;

public record UsuarioActualResponse(
        Long id,
        String nombres,
        String correo,
        String rol,
        Long areaId,
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
