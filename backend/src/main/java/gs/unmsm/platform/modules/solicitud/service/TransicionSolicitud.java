package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.usuario.model.RolNombre;

import java.util.Optional;

/**
 * Siguiente paso permitido según el rol y el estado actual.
 * Pendiente → En revisión → Asignado → En proceso → Resuelto → Cerrado.
 */
public final class TransicionSolicitud {

    public record Paso(String destino, boolean requiereTecnico, boolean requiereNota) {
    }

    private TransicionSolicitud() {
    }

    public static Optional<Paso> siguiente(String rol, String estadoActual) {
        return switch (rol) {
            case RolNombre.SUPERVISOR, RolNombre.ADMINISTRADOR -> switch (estadoActual) {
                case EstadoNombre.PENDIENTE -> Optional.of(paso(EstadoNombre.EN_REVISION, false, false));
                case EstadoNombre.EN_REVISION -> Optional.of(paso(EstadoNombre.ASIGNADO, true, false));
                case EstadoNombre.EN_PROCESO -> Optional.of(paso(EstadoNombre.RESUELTO, false, true));
                case EstadoNombre.RESUELTO -> Optional.of(paso(EstadoNombre.CERRADO, false, false));
                default -> Optional.empty();
            };
            case RolNombre.TECNICO -> switch (estadoActual) {
                case EstadoNombre.ASIGNADO -> Optional.of(paso(EstadoNombre.EN_PROCESO, false, false));
                case EstadoNombre.EN_PROCESO -> Optional.of(paso(EstadoNombre.RESUELTO, false, true));
                default -> Optional.empty();
            };
            case RolNombre.ESTUDIANTE -> EstadoNombre.RESUELTO.equals(estadoActual)
                    ? Optional.of(paso(EstadoNombre.CERRADO, false, false))
                    : Optional.empty();
            default -> Optional.empty();
        };
    }

    private static Paso paso(String destino, boolean requiereTecnico, boolean requiereNota) {
        return new Paso(destino, requiereTecnico, requiereNota);
    }
}
