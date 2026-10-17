package gs.unmsm.platform.modules.solicitud.dto;

import gs.unmsm.platform.modules.solicitud.model.Prioridad;

public record PrioridadResponse(Long id, String nombre, int slaDias, boolean diasHabiles) {

    public static PrioridadResponse from(Prioridad prioridad) {
        return new PrioridadResponse(prioridad.getId(), prioridad.getNombre(), prioridad.getSlaDias(),
                Boolean.TRUE.equals(prioridad.getDiasHabiles()));
    }
}
