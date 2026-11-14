package gs.unmsm.platform.modules.dashboard;

import com.jayway.jsonpath.JsonPath;
import gs.unmsm.platform.common.security.JwtService;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.repository.CategoriaRepository;
import gs.unmsm.platform.modules.solicitud.repository.PrioridadRepository;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.storage.evidencias-dir=target/test-evidencias")
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired PrioridadRepository prioridades;

    @Test
    void elAlcanceDelDashboardDependeDelRol() throws Exception {
        String andrea = tokenDe("andrea.mendoza@unmsm.edu.pe");
        String ronald = tokenDe("ronald.munayco@unmsm.edu.pe");
        String diana = tokenDe("diana.cardenas@unmsm.edu.pe");
        long dianaId = usuarios.findByCorreoInstitucional("diana.cardenas@unmsm.edu.pe").orElseThrow().getId();

        String codigo = registrar(andrea);
        avanzar(ronald, codigo, "{\"estado\":\"En revisión\"}");
        avanzar(ronald, codigo, "{\"estado\":\"Asignado\",\"tecnicoId\":" + dianaId + "}");

        mvc.perform(get("/api/dashboard").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.porEstado[?(@.nombre == 'Asignado')].cantidad").isNotEmpty())
                .andExpect(jsonPath("$.porPrioridad").isArray())
                .andExpect(jsonPath("$.porArea").isArray());

        mvc.perform(get("/api/dashboard").header("Authorization", diana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.porEstado[?(@.nombre == 'Asignado')].cantidad").isNotEmpty());

        mvc.perform(get("/api/dashboard").header("Authorization", ronald))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.porArea[0].nombre").value("Mantenimiento general"));

        String admin = tokenDe("marcos.vidal@unmsm.edu.pe");
        mvc.perform(get("/api/dashboard").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.porArea.length()").value(greaterThanOrEqualTo(1)));

        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
    }

    private String tokenDe(String correo) {
        Usuario u = usuarios.findByCorreoInstitucional(correo).orElseThrow();
        Long areaId = u.getArea() == null ? null : u.getArea().getId();
        return "Bearer " + jwtService.emitir(
                new UsuarioAutenticado(u.getId(), u.getNombres(), u.getRol().getNombre(), areaId)).token();
    }

    private String registrar(String token) throws Exception {
        long categoria = categorias.findByNombre("Mantenimiento").orElseThrow().getId();
        long prioridad = prioridades.findByNombre("Media").orElseThrow().getId();
        String cuerpo = """
                {"categoriaId":%d,"prioridadId":%d,"asunto":"Fuga en el lavadero",
                "descripcion":"El lavadero del primer piso pierde agua.","pabellon":"Pabellón A","nivel":"1","ambiente":"SSHH"}
                """.formatted(categoria, prioridad);
        return JsonPath.read(mvc.perform(multipart("/api/solicitudes")
                        .file(new MockMultipartFile("solicitud", "", "application/json",
                                cuerpo.getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.codigo");
    }

    private void avanzar(String token, String codigo, String cuerpo) throws Exception {
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk());
    }
}
