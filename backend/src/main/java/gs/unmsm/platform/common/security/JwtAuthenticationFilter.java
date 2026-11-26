package gs.unmsm.platform.common.security;

import gs.unmsm.platform.common.observabilidad.MdcUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER)) {
            jwtService.validar(header.substring(BEARER.length())).ifPresent(usuario -> {
                var auth = UsernamePasswordAuthenticationToken.authenticated(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())));
                SecurityContextHolder.getContext().setAuthentication(auth);
                // El filtro de correlación limpia estas claves al terminar la petición.
                MDC.put(MdcUsuario.USUARIO_ID, String.valueOf(usuario.id()));
                MDC.put(MdcUsuario.ROL, usuario.rol());
            });
        }
        chain.doFilter(request, response);
    }
}
