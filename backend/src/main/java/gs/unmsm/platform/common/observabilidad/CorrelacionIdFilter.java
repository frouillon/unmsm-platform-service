package gs.unmsm.platform.common.observabilidad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Asigna un identificador de correlación a cada petición y deja un registro de acceso por línea.
 *
 * <p>Si el cliente envía {@code X-Correlation-Id} con un formato seguro se reutiliza; si no, se genera uno.
 * El valor queda en el MDC (aparece en todas las líneas de log de la petición) y se devuelve en la respuesta
 * para que el frontend o quien reporte un error pueda citarlo.</p>
 *
 * <p>El registro de acceso no incluye query string, cuerpo ni cabeceras: así no se filtran datos personales
 * ni tokens a los logs.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelacionIdFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Correlation-Id";
    public static final String MDC_CLAVE = "correlationId";

    /** Solo letras, dígitos y guiones, hasta 64 caracteres: evita inyección de líneas en los logs. */
    private static final Pattern FORMATO_SEGURO = Pattern.compile("[A-Za-z0-9-]{8,64}");

    private static final Logger ACCESO = LoggerFactory.getLogger("gs.unmsm.platform.acceso");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String correlacion = resolver(request.getHeader(CABECERA));
        long inicio = System.nanoTime();
        MDC.put(MDC_CLAVE, correlacion);
        response.setHeader(CABECERA, correlacion);
        try {
            chain.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            if (debeRegistrar(request.getRequestURI())) {
                ACCESO.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(),
                        response.getStatus(), ms);
            }
            MDC.remove(MDC_CLAVE);
            MDC.remove(MdcUsuario.USUARIO_ID);
            MDC.remove(MdcUsuario.ROL);
        }
    }

    static String resolver(String recibido) {
        if (recibido != null && FORMATO_SEGURO.matcher(recibido).matches()) {
            return recibido;
        }
        return UUID.randomUUID().toString();
    }

    /** El healthcheck de Docker consulta cada 15 s; registrarlo solo ensucia los logs. */
    private static boolean debeRegistrar(String uri) {
        return uri == null || !uri.startsWith("/actuator/health");
    }
}
