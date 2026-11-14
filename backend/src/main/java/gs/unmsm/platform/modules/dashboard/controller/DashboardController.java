package gs.unmsm.platform.modules.dashboard.controller;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.dashboard.dto.DashboardResponse;
import gs.unmsm.platform.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Indicadores agregados según el rol del usuario.")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboard;

    @GetMapping
    @Operation(
            summary = "Indicadores del alcance actual",
            description = "Devuelve totales y distribuciones por estado, área y prioridad. "
                    + "El alcance es: propias (estudiante), asignadas (técnico), del área (supervisor) o todas (administrador).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicadores del alcance."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    public DashboardResponse indicadores(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return dashboard.indicadores(usuario);
    }
}
