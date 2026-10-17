package gs.unmsm.platform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "Ingresa tu correo institucional.")
        @Pattern(regexp = "^\\s*[^@\\s]+@unmsm\\.edu\\.pe\\s*$", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Usa tu correo institucional @unmsm.edu.pe.")
        String correo,

        @NotBlank(message = "Ingresa tu contraseña.")
        String password
) {
}
