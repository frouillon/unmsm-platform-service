package gs.unmsm.platform.common.observabilidad;

import gs.unmsm.platform.common.security.JwtService;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.storage.evidencias-dir=target/test-evidencias")
@AutoConfigureMockMvc
@Transactional
class ObservabilidadTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;

    @Test
    void cadaRespuestaLlevaUnIdDeCorrelacion() throws Exception {
        mvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(CorrelacionIdFilter.CABECERA, matchesPattern("[0-9a-f-]{36}")));

        mvc.perform(get("/api/dashboard").header(CorrelacionIdFilter.CABECERA, "prueba-1234"))
                .andExpect(header().string(CorrelacionIdFilter.CABECERA, "prueba-1234"));
    }

    @Test
    void ignoraIdsDeCorrelacionConFormatoInseguro() {
        assertThat(CorrelacionIdFilter.resolver("abc\r\nlinea-falsa")).isNotEqualTo("abc\r\nlinea-falsa");
        assertThat(CorrelacionIdFilter.resolver("corto")).hasSize(36);
        assertThat(CorrelacionIdFilter.resolver(null)).hasSize(36);
        assertThat(CorrelacionIdFilter.resolver("a".repeat(65))).hasSize(36);
        assertThat(CorrelacionIdFilter.resolver("frontend-0f3c9a2b")).isEqualTo("frontend-0f3c9a2b");
    }

    @Test
    void healthEsPublicoYLasMetricasSoloParaAdministradores() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
        mvc.perform(get("/actuator/info")).andExpect(status().isOk());

        mvc.perform(get("/actuator/prometheus")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics").header("Authorization", tokenDe("ronald.munayco@unmsm.edu.pe")))
                .andExpect(status().isForbidden());

        String admin = tokenDe("marcos.vidal@unmsm.edu.pe");
        mvc.perform(get("/actuator/metrics").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
        // Las métricas de negocio quedan registradas desde el arranque. (El formato Prometheus no se prueba
        // aquí porque Spring Boot desactiva la exportación de métricas en los tests.)
        mvc.perform(get("/actuator/metrics/sanmarcos.sla.alertas").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("sanmarcos.sla.alertas"));
    }

    private String tokenDe(String correo) {
        Usuario u = usuarios.findByCorreoInstitucional(correo).orElseThrow();
        Long areaId = u.getArea() == null ? null : u.getArea().getId();
        return "Bearer " + jwtService.emitir(
                new UsuarioAutenticado(u.getId(), u.getNombres(), u.getRol().getNombre(), areaId)).token();
    }
}
