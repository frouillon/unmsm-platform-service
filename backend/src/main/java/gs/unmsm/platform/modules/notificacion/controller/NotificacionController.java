package gs.unmsm.platform.modules.notificacion.controller;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.notificacion.dto.NotificacionResponse;
import gs.unmsm.platform.modules.notificacion.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Avisos in-app sobre solicitudes.")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificaciones;

    @GetMapping
    @Operation(summary = "Listar mis notificaciones", description = "Devuelve los avisos del usuario autenticado, más recientes primero.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de notificaciones."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    public List<NotificacionResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return notificaciones.listar(usuario);
    }

    @GetMapping("/no-leidas")
    @Operation(summary = "Contar no leídas", description = "Cantidad de notificaciones sin leer del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteo de no leídas."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    public Map<String, Long> noLeidas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return Map.of("cantidad", notificaciones.contarNoLeidas(usuario));
    }

    @PostMapping("/{id}/leer")
    @Operation(summary = "Marcar una notificación como leída")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación marcada."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public Map<String, Boolean> leer(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador de la notificación.", example = "1")
            @PathVariable Long id) {
        notificaciones.marcarLeida(usuario, id);
        return Map.of("ok", true);
    }

    @PostMapping("/leer-todas")
    @Operation(summary = "Marcar todas como leídas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cantidad de notificaciones actualizadas."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    public Map<String, Integer> leerTodas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return Map.of("actualizadas", notificaciones.marcarTodasLeidas(usuario));
    }
}
