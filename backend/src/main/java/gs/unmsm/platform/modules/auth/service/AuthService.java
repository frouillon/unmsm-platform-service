package gs.unmsm.platform.modules.auth.service;

import gs.unmsm.platform.common.exception.ForbiddenException;
import gs.unmsm.platform.common.exception.UnauthorizedException;
import gs.unmsm.platform.common.security.JwtService;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.auth.dto.LoginRequest;
import gs.unmsm.platform.modules.auth.dto.LoginResponse;
import gs.unmsm.platform.modules.auth.dto.UsuarioActualResponse;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    static final String CREDENCIALES_INVALIDAS =
            "Correo o contraseña incorrectos. Verifica tus datos e inténtalo de nuevo.";
    static final String CUENTA_INACTIVA =
            "Tu cuenta está inactiva. Comunícate con el administrador del sistema.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashSenuelo;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashSenuelo = passwordEncoder.encode("hash-senuelo");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        Optional<Usuario> encontrado = usuarioRepository.findByCorreoInstitucional(correo);

        // Se compara contra un hash señuelo cuando el correo no existe para no revelar cuentas por tiempo de respuesta.
        String hash = encontrado.map(Usuario::getPasswordHash).orElse(hashSenuelo);
        boolean passwordValida = passwordEncoder.matches(request.password(), hash);
        if (encontrado.isEmpty() || !passwordValida) {
            throw new UnauthorizedException(CREDENCIALES_INVALIDAS);
        }

        Usuario usuario = encontrado.get();
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ForbiddenException(CUENTA_INACTIVA);
        }

        UsuarioActualResponse actual = UsuarioActualResponse.from(usuario);
        JwtService.TokenEmitido token = jwtService.emitir(
                new UsuarioAutenticado(actual.id(), actual.nombres(), actual.rol(), actual.areaId()));
        return new LoginResponse(token.token(), "Bearer", token.expiraEn(), actual);
    }

    @Transactional(readOnly = true)
    public UsuarioActualResponse usuarioActual(UsuarioAutenticado autenticado) {
        return usuarioRepository.findById(autenticado.id())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .map(UsuarioActualResponse::from)
                .orElseThrow(() -> new UnauthorizedException("Tu sesión ya no es válida. Inicia sesión nuevamente."));
    }
}
