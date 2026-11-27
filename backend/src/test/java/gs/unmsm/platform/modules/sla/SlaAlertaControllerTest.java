package gs.unmsm.platform.modules.sla;

import gs.unmsm.platform.common.security.JwtService;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.sla.service.SlaAlertaJob;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.storage.evidencias-dir=target/test-evidencias")
@AutoConfigureMockMvc
@Transactional
class SlaAlertaControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired ApplicationContext contexto;

    @Test
    void soloElAdministradorEjecutaLosProcesosManualmente() throws Exception {
        String admin = tokenDe("marcos.vidal@unmsm.edu.pe");

        mvc.perform(post("/api/admin/sla/evaluar").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisadas").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.alertasEmitidas").value(greaterThanOrEqualTo(0)));
        mvc.perform(post("/api/admin/sla/consolidado").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enviados").value(greaterThanOrEqualTo(0)));

        mvc.perform(post("/api/admin/sla/evaluar").header("Authorization", tokenDe("ronald.munayco@unmsm.edu.pe")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/sla/evaluar")).andExpect(status().isUnauthorized());
    }

    @Test
    void enPruebasElPlanificadorEstaApagado() {
        // pom.xml define app.sla.alertas.habilitado=false para las pruebas.
        assertThat(contexto.getBeanNamesForType(SlaAlertaJob.class)).isEmpty();
    }

    private String tokenDe(String correo) {
        Usuario u = usuarios.findByCorreoInstitucional(correo).orElseThrow();
        Long areaId = u.getArea() == null ? null : u.getArea().getId();
        return "Bearer " + jwtService.emitir(
                new UsuarioAutenticado(u.getId(), u.getNombres(), u.getRol().getNombre(), areaId)).token();
    }
}
