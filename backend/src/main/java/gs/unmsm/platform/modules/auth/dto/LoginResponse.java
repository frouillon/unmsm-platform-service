package gs.unmsm.platform.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Respuesta del inicio de sesión con el token JWT y los datos del usuario.")
public record LoginResponse(
        @Schema(description = "Token JWT para autenticar las siguientes peticiones.",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.firma")
        String token,

        @Schema(description = "Tipo de autorización que debe usarse en la cabecera Authorization.",
                example = "Bearer")
        String tipo,

        @Schema(description = "Fecha y hora en que expira el token.", example = "2026-11-14T23:59:59Z")
        Instant expiraEn,

        @Schema(description = "Datos del usuario autenticado.")
        UsuarioActualResponse usuario
) {
}
