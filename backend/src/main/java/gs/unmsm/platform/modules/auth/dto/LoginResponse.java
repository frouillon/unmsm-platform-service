package gs.unmsm.platform.modules.auth.dto;

import java.time.Instant;

public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEn,
        UsuarioActualResponse usuario
) {
}
