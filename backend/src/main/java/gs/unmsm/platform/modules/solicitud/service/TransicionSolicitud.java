package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.usuario.model.RolNombre;

import java.util.Optional;

/**
 * Siguiente paso permitido según el rol y el estado actual.
 * Pendiente → En revisión → Asignado → En proceso → Resuelto → Cerrado.
 */
public final class TransicionSolicitud {

    public record Paso(String destino, boolean requiereTecnico) {
    }

    private TransicionSolicitud() {
    }

    public static Optional<Paso> siguiente(String rol, String estadoActual) {
        return switch (rol) {
            case RolNombre.SUPERVISOR, RolNombre.ADMINISTRADOR -> switch (estadoActual) {
                case EstadoNombre.PENDIENTE -> Optional.of(new Paso(EstadoNombre.EN_REVISION, false));
                case EstadoNombre.EN_REVISION -> Optional.of(new Paso(EstadoNombre.ASIGNADO, true));
                default -> Optional.empty();
            };
            case RolNombre.TECNICO -> switch (estadoActual) {
                case EstadoNombre.ASIGNADO -> Optional.of(new Paso(EstadoNombre.EN_PROCESO, false));
                case EstadoNombre.EN_PROCESO -> Optional.of(new Paso(EstadoNombre.RESUELTO, false));
                default -> Optional.empty();
            };
            case RolNombre.ESTUDIANTE -> EstadoNombre.RESUELTO.equals(estadoActual)
                    ? Optional.of(new Paso(EstadoNombre.CERRADO, false))
                    : Optional.empty();
            default -> Optional.empty();
        };
    }
}
