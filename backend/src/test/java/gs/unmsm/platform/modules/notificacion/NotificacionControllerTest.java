package gs.unmsm.platform.modules.notificacion;

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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.storage.evidencias-dir=target/test-evidencias")
@AutoConfigureMockMvc
@Transactional
class NotificacionControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired PrioridadRepository prioridades;

    @Test
    void elCambioDeEstadoGeneraAvisosYSePuedenMarcarComoLeidos() throws Exception {
        String andrea = tokenDe("andrea.mendoza@unmsm.edu.pe");
        String ronald = tokenDe("ronald.munayco@unmsm.edu.pe");
        String diana = tokenDe("diana.cardenas@unmsm.edu.pe");
        long dianaId = usuarios.findByCorreoInstitucional("diana.cardenas@unmsm.edu.pe").orElseThrow().getId();

        String codigo = registrar(andrea);

        mvc.perform(get("/api/notificaciones").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        avanzar(ronald, codigo, "{\"estado\":\"En revisión\"}");

        mvc.perform(get("/api/notificaciones/no-leidas").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(1));
        String listado = mvc.perform(get("/api/notificaciones").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("CAMBIO_ESTADO"))
                .andExpect(jsonPath("$[0].codigoSolicitud").value(codigo))
                .andExpect(jsonPath("$[0].leida").value(false))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(listado, "$[0].id");

        mvc.perform(post("/api/notificaciones/" + id + "/leer").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        mvc.perform(get("/api/notificaciones/no-leidas").header("Authorization", andrea))
                .andExpect(jsonPath("$.cantidad").value(0));

        avanzar(ronald, codigo, "{\"estado\":\"Asignado\",\"tecnicoId\":" + dianaId + "}");

        mvc.perform(get("/api/notificaciones").header("Authorization", diana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("ASIGNACION"))
                .andExpect(jsonPath("$[0].leida").value(false));
        mvc.perform(get("/api/notificaciones").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("CAMBIO_ESTADO"))
                .andExpect(jsonPath("$[0].mensaje").value("La solicitud " + codigo + " pasó a Asignado."));

        mvc.perform(post("/api/solicitudes/" + codigo + "/comentarios").header("Authorization", diana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contenido\":\"Ya estoy en camino al aula.\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/notificaciones").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("COMENTARIO"));

        mvc.perform(post("/api/notificaciones/leer-todas").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actualizadas").value(2));
        mvc.perform(get("/api/notificaciones/no-leidas").header("Authorization", andrea))
                .andExpect(jsonPath("$.cantidad").value(0));

        mvc.perform(get("/api/notificaciones").header("Authorization", ronald))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/notificaciones")).andExpect(status().isUnauthorized());
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
