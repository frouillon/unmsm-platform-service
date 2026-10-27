package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.usuario.model.RolNombre;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TransicionEstados {

    private static final Map<String, Map<String, Set<String>>> REGLAS = Map.of(
            EstadoNombre.PENDIENTE, Map.of(
                    RolNombre.SUPERVISOR, Set.of(EstadoNombre.EN_PROCESO),
                    RolNombre.ADMINISTRADOR, Set.of(EstadoNombre.EN_PROCESO, EstadoNombre.CERRADO)
            ),
            EstadoNombre.EN_PROCESO, Map.of(
                    RolNombre.TECNICO, Set.of(EstadoNombre.RESUELTO),
                    RolNombre.SUPERVISOR, Set.of(EstadoNombre.RESUELTO, EstadoNombre.CERRADO),
                    RolNombre.ADMINISTRADOR, Set.of(EstadoNombre.RESUELTO, EstadoNombre.CERRADO)
            ),
            EstadoNombre.RESUELTO, Map.of(
                    RolNombre.SUPERVISOR, Set.of(EstadoNombre.CERRADO),
                    RolNombre.ADMINISTRADOR, Set.of(EstadoNombre.CERRADO)
            ),
            EstadoNombre.CERRADO, Map.of()
    );

    private TransicionEstados() {
    }

    public static boolean permitido(String estadoOrigen, String rol, String estadoDestino) {
        if (estadoOrigen == null || rol == null || estadoDestino == null) {
            return false;
        }
        if (estadoOrigen.equals(estadoDestino)) {
            return false;
        }
        Map<String, Set<String>> porRol = REGLAS.get(estadoOrigen);
        if (porRol == null) {
            return false;
        }
        Set<String> destinos = porRol.get(rol);
        if (destinos == null || destinos.isEmpty()) {
            return false;
        }
        return destinos.contains(estadoDestino);
    }

    public static List<String> destinosPermitidos(String estadoOrigen, String rol) {
        if (estadoOrigen == null || rol == null) {
            return List.of();
        }
        Map<String, Set<String>> porRol = REGLAS.get(estadoOrigen);
        if (porRol == null) {
            return List.of();
        }
        Set<String> destinos = porRol.get(rol);
        if (destinos == null || destinos.isEmpty()) {
            return List.of();
        }
        return destinos.stream().toList();
    }

    public static boolean requiereNotaTecnica(String estadoDestino) {
        return EstadoNombre.RESUELTO.equals(estadoDestino) || EstadoNombre.CERRADO.equals(estadoDestino);
    }
}
