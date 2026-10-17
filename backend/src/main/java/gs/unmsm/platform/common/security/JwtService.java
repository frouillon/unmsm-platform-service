package gs.unmsm.platform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final String CLAIM_NOMBRES = "nombres";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_AREA = "areaId";

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public TokenEmitido emitir(UsuarioAutenticado usuario) {
        Instant ahora = Instant.now();
        Instant expiraEn = ahora.plus(expiration);
        String token = Jwts.builder()
                .subject(String.valueOf(usuario.id()))
                .claim(CLAIM_NOMBRES, usuario.nombres())
                .claim(CLAIM_ROLE, usuario.rol())
                .claim(CLAIM_AREA, usuario.areaId())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiraEn))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new TokenEmitido(token, expiraEn);
    }

    /**
     * Devuelve vacío si el token está mal formado, tiene firma inválida o expiró.
     */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Number areaId = claims.get(CLAIM_AREA, Number.class);
            return Optional.of(new UsuarioAutenticado(
                    Long.valueOf(claims.getSubject()),
                    claims.get(CLAIM_NOMBRES, String.class),
                    claims.get(CLAIM_ROLE, String.class),
                    areaId == null ? null : areaId.longValue()));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public record TokenEmitido(String token, Instant expiraEn) {
    }
}
