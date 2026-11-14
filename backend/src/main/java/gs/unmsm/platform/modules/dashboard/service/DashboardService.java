package gs.unmsm.platform.modules.dashboard.service;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.dashboard.dto.ConteoItem;
import gs.unmsm.platform.modules.dashboard.dto.DashboardResponse;
import gs.unmsm.platform.modules.dashboard.repository.DashboardRepository;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboard;
    private final Clock clock;

    @Transactional(readOnly = true)
    public DashboardResponse indicadores(UsuarioAutenticado usuario) {
        Long solicitanteId = null;
        Long tecnicoId = null;
        Long areaId = null;

        switch (usuario.rol()) {
            case RolNombre.ESTUDIANTE -> solicitanteId = usuario.id();
            case RolNombre.TECNICO -> tecnicoId = usuario.id();
            case RolNombre.SUPERVISOR -> areaId = usuario.areaId();
            case RolNombre.ADMINISTRADOR -> {
                // Alcance global: sin filtros.
            }
            default -> {
                return vacio();
            }
        }

        if (RolNombre.SUPERVISOR.equals(usuario.rol()) && areaId == null) {
            return vacio();
        }

        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        return new DashboardResponse(
                dashboard.total(solicitanteId, tecnicoId, areaId),
                dashboard.vencidas(solicitanteId, tecnicoId, areaId, ahora),
                mapear(dashboard.porEstado(solicitanteId, tecnicoId, areaId)),
                mapear(dashboard.porArea(solicitanteId, tecnicoId, areaId)),
                mapear(dashboard.porPrioridad(solicitanteId, tecnicoId, areaId)));
    }

    private static List<ConteoItem> mapear(List<Object[]> filas) {
        return filas.stream()
                .map(f -> new ConteoItem((String) f[0], (Long) f[1]))
                .toList();
    }

    private static DashboardResponse vacio() {
        return new DashboardResponse(0, 0, List.of(), List.of(), List.of());
    }
}
