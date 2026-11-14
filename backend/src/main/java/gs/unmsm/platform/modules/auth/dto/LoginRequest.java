package gs.unmsm.platform.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Credenciales para iniciar sesión.")
public record LoginRequest(
        @Schema(description = "Correo institucional terminado en @unmsm.edu.pe.",
                example = "andrea.mendoza@unmsm.edu.pe",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Ingresa tu correo institucional.")
        @Pattern(regexp = "^\\s*[^@\\s]+@unmsm\\.edu\\.pe\\s*$", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Usa tu correo institucional @unmsm.edu.pe.")
        String correo,

        @Schema(description = "Contraseña de la cuenta.",
                example = "SanMarcos2026",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Ingresa tu contraseña.")
        String password
) {
}
