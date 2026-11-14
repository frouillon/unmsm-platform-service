package gs.unmsm.platform.modules.auth.controller;

import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.auth.dto.LoginRequest;
import gs.unmsm.platform.modules.auth.dto.LoginResponse;
import gs.unmsm.platform.modules.auth.dto.UsuarioActualResponse;
import gs.unmsm.platform.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y datos del usuario autenticado.")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales institucionales y devuelve un token JWT. "
                    + "El token debe enviarse en las demás peticiones como `Authorization: Bearer <token>`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas; devuelve el token y el usuario."),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Obtener usuario autenticado",
            description = "Devuelve los datos del usuario dueño del token enviado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos del usuario autenticado."),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    })
    public UsuarioActualResponse me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return authService.usuarioActual(usuario);
    }
}
