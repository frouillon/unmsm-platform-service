package gs.unmsm.platform.modules.auth.service;

import gs.unmsm.platform.common.exception.ForbiddenException;
import gs.unmsm.platform.common.exception.UnauthorizedException;
import gs.unmsm.platform.common.security.JwtService;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.auth.dto.LoginRequest;
import gs.unmsm.platform.modules.auth.dto.LoginResponse;
import gs.unmsm.platform.modules.usuario.model.Rol;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-01 · RF-01 a RF-03 — Inicio de sesión.
 * <p>
 * Técnica: caja blanca, cobertura de ramas de {@link AuthService#login}. El repositorio de usuarios
 * y el emisor de tokens se sustituyen con Mockito, así que la prueba no necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String CORREO = "ana.torres@unmsm.edu.pe";
    private static final String CLAVE = "SanMarcos2026";
    private static final int COSTO_MINIMO_DE_BCRYPT = 4;

    @Mock
    UsuarioRepository usuarios;
    @Mock
    JwtService jwtService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(COSTO_MINIMO_DE_BCRYPT);
    private AuthService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new AuthService(usuarios, encoder, jwtService);
    }

    @Test
    void elCorreoSeNormalizaAntesDeBuscarAlUsuario() {
        when(usuarios.findByCorreoInstitucional(CORREO)).thenReturn(Optional.of(usuario(true)));
        when(jwtService.emitir(any(UsuarioAutenticado.class)))
                .thenReturn(new JwtService.TokenEmitido("token-de-prueba", Instant.parse("2026-10-20T23:00:00Z")));

        LoginResponse respuesta = servicio.login(new LoginRequest("  Ana.Torres@UNMSM.edu.pe ", CLAVE));

        assertThat(respuesta.token()).isEqualTo("token-de-prueba");
        assertThat(respuesta.usuario().correo()).isEqualTo(CORREO);
        assertThat(respuesta.usuario().rol()).isEqualTo(RolNombre.ESTUDIANTE);
    }

    @Test
    void correoInexistenteYClaveIncorrectaDanElMismoErrorYNoEmitenToken() {
        when(usuarios.findByCorreoInstitucional("nadie@unmsm.edu.pe")).thenReturn(Optional.empty());
        when(usuarios.findByCorreoInstitucional(CORREO)).thenReturn(Optional.of(usuario(true)));
        String errorGenerico = "Correo o contraseña incorrectos. Verifica tus datos e inténtalo de nuevo.";

        assertThatThrownBy(() -> servicio.login(new LoginRequest("nadie@unmsm.edu.pe", CLAVE)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(errorGenerico);
        assertThatThrownBy(() -> servicio.login(new LoginRequest(CORREO, "clave-equivocada")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage(errorGenerico);

        verify(jwtService, never()).emitir(any(UsuarioAutenticado.class));
    }

    @Test
    void unaCuentaInactivaEsRechazadaAunqueLaClaveSeaCorrecta() {
        when(usuarios.findByCorreoInstitucional(CORREO)).thenReturn(Optional.of(usuario(false)));

        assertThatThrownBy(() -> servicio.login(new LoginRequest(CORREO, CLAVE)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("inactiva");

        verify(jwtService, never()).emitir(any(UsuarioAutenticado.class));
    }

    private Usuario usuario(boolean activo) {
        return Usuario.builder()
                .id(1L)
                .nombres("Ana Torres")
                .correoInstitucional(CORREO)
                .passwordHash(encoder.encode(CLAVE))
                .rol(Rol.builder().nombre(RolNombre.ESTUDIANTE).build())
                .activo(activo)
                .build();
    }
}
