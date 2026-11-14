package gs.unmsm.platform.common.security;

import gs.unmsm.platform.modules.usuario.model.RolNombre;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF-02 · HU-02 — Emisión y validación del token JWT.
 * <p>
 * Técnicas: partición de equivalencia (token válido, expirado o que no es un token)
 * y conjetura de errores (token manipulado por un atacante).
 */
class JwtServiceTest {

    private static final String SECRETO = secreto("clave-solo-para-pruebas-unitarias-numero-1");
    private static final String OTRO_SECRETO = secreto("clave-solo-para-pruebas-unitarias-numero-2");
    private static final long OCHO_HORAS = 480;
    private static final long YA_EXPIRADO = -1;

    private static final UsuarioAutenticado ANDREA = new UsuarioAutenticado(7L, "Andrea Mendoza", RolNombre.ESTUDIANTE, null);
    private static final UsuarioAutenticado CARLOS = new UsuarioAutenticado(8L, "Carlos Quispe", RolNombre.TECNICO, 3L);

    private final JwtService jwt = new JwtService(SECRETO, OCHO_HORAS);

    @Test
    void unTokenRecienEmitidoDevuelveLosMismosDatosDelUsuario() {
        assertThat(jwt.validar(jwt.emitir(ANDREA).token())).contains(ANDREA);
        assertThat(jwt.validar(jwt.emitir(CARLOS).token())).contains(CARLOS);
    }

    @Test
    void unTokenExpiradoEsRechazado() {
        String expirado = new JwtService(SECRETO, YA_EXPIRADO).emitir(ANDREA).token();

        assertThat(jwt.validar(expirado)).isEmpty();
    }

    @Test
    void unTokenConElRolAlteradoEsRechazado() {
        String original = jwt.emitir(ANDREA).token();

        String falsificado = cambiarEnElContenido(original, RolNombre.ESTUDIANTE, RolNombre.ADMINISTRADOR);

        assertThat(falsificado).isNotEqualTo(original);
        assertThat(jwt.validar(falsificado)).isEmpty();
    }

    @Test
    void unTokenFirmadoConOtraClaveEsRechazado() {
        String ajeno = new JwtService(OTRO_SECRETO, OCHO_HORAS).emitir(ANDREA).token();

        assertThat(jwt.validar(ajeno)).isEmpty();
    }

    @Test
    void unTextoQueNoEsUnTokenEsRechazado() {
        assertThat(jwt.validar("esto-no-es-un-token")).isEmpty();
        assertThat(jwt.validar("")).isEmpty();
    }

    /** Clave en Base64, como la espera la aplicación (mínimo 256 bits). */
    private static String secreto(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reescribe el contenido del token (su segunda parte) conservando la firma original,
     * que es lo que haría alguien que intenta darse más permisos.
     */
    private static String cambiarEnElContenido(String token, String buscado, String reemplazo) {
        String[] partes = token.split("\\.");
        String contenido = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        String alterado = contenido.replace(buscado, reemplazo);
        partes[1] = Base64.getUrlEncoder().withoutPadding().encodeToString(alterado.getBytes(StandardCharsets.UTF_8));
        return String.join(".", partes);
    }
}
