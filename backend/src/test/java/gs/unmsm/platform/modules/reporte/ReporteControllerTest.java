package gs.unmsm.platform.modules.reporte;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.storage.evidencias-dir=target/test-evidencias")
@AutoConfigureMockMvc
@Transactional
class ReporteControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired PrioridadRepository prioridades;

    @Test
    void elSupervisorObtieneElReporteDeSuAreaEnJsonYCsv() throws Exception {
        String andrea = tokenDe("andrea.mendoza@unmsm.edu.pe");
        String ronald = tokenDe("ronald.munayco@unmsm.edu.pe");
        String codigo = registrar(andrea);

        mvc.perform(get("/api/reportes/solicitudes").header("Authorization", ronald))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alcance").value("Mantenimiento general"))
                .andExpect(jsonPath("$.resumen.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.filas[*].codigo").value(hasItem(codigo)))
                .andExpect(jsonPath("$.filas[?(@.codigo == '" + codigo + "')].cumplimiento").value(hasItem("En plazo")));

        mvc.perform(get("/api/reportes/solicitudes/csv").header("Authorization", ronald))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString(".csv")))
                .andExpect(content().string(containsString(codigo)));

        mvc.perform(get("/api/reportes/areas").header("Authorization", ronald))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Mantenimiento general"));
    }

    @Test
    void elAdministradorVeTodasLasAreas() throws Exception {
        String admin = tokenDe("marcos.vidal@unmsm.edu.pe");

        mvc.perform(get("/api/reportes/solicitudes").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alcance").value("Todas las áreas"));
        mvc.perform(get("/api/reportes/areas").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(5)));
        mvc.perform(get("/api/reportes/solicitudes").param("areaId", "999999").header("Authorization", admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaRolesSinPermisoYFiltrosInvalidos() throws Exception {
        mvc.perform(get("/api/reportes/solicitudes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/reportes/solicitudes").header("Authorization", tokenDe("andrea.mendoza@unmsm.edu.pe")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/reportes/solicitudes/csv").header("Authorization", tokenDe("diana.cardenas@unmsm.edu.pe")))
                .andExpect(status().isForbidden());

        String ronald = tokenDe("ronald.munayco@unmsm.edu.pe");
        mvc.perform(get("/api/reportes/solicitudes").param("desde", "2026-11-30").param("hasta", "2026-11-01")
                        .header("Authorization", ronald))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.desde").exists());
        mvc.perform(get("/api/reportes/solicitudes").param("desde", "ayer").header("Authorization", ronald))
                .andExpect(status().isBadRequest());
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
                {"categoriaId":%d,"prioridadId":%d,"asunto":"Puerta del aula no cierra",
                "descripcion":"La puerta del aula 204 no cierra desde el lunes.","pabellon":"Pabellón B","nivel":"2","ambiente":"204"}
                """.formatted(categoria, prioridad);
        return JsonPath.read(mvc.perform(multipart("/api/solicitudes")
                        .file(new MockMultipartFile("solicitud", "", "application/json",
                                cuerpo.getBytes(StandardCharsets.UTF_8)))
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.codigo");
    }
}
