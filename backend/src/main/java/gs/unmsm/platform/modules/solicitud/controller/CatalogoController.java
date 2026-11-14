package gs.unmsm.platform.modules.solicitud.controller;

import gs.unmsm.platform.modules.solicitud.dto.CategoriaResponse;
import gs.unmsm.platform.modules.solicitud.dto.PrioridadResponse;
import gs.unmsm.platform.modules.solicitud.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@Tag(name = "Catálogos", description = "Categorías y prioridades disponibles para registrar solicitudes.")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/categorias")
    @Operation(
            summary = "Listar categorías activas",
            description = "Devuelve las categorías disponibles para registrar una solicitud e indica "
                    + "si cada una exige ubicación (pabellón, nivel y ambiente). Requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de categorías activas."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    public List<CategoriaResponse> categorias() {
        return catalogoService.categoriasActivas();
    }

    @GetMapping("/prioridades")
    @Operation(
            summary = "Listar prioridades",
            description = "Devuelve las prioridades con su SLA en días y si el plazo se cuenta en días hábiles. "
                    + "Requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de prioridades."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    public List<PrioridadResponse> prioridades() {
        return catalogoService.prioridades();
    }
}
