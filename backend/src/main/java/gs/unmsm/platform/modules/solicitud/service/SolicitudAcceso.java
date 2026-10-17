package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.usuario.model.RolNombre;

import java.util.Objects;

/** Quién puede ver una solicitud según su rol. */
public final class SolicitudAcceso {

    private SolicitudAcceso() {
    }

    public static boolean puedeVer(UsuarioAutenticado usuario, Solicitud solicitud) {
        return switch (usuario.rol()) {
            case RolNombre.ESTUDIANTE -> Objects.equals(solicitud.getSolicitante().getId(), usuario.id());
            case RolNombre.TECNICO -> solicitud.getTecnico() != null
                    && Objects.equals(solicitud.getTecnico().getId(), usuario.id());
            case RolNombre.SUPERVISOR -> Objects.equals(solicitud.getArea().getId(), usuario.areaId());
            case RolNombre.ADMINISTRADOR -> true;
            default -> false;
        };
    }
}
