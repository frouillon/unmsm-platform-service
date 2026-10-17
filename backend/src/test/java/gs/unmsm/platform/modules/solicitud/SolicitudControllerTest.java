package gs.unmsm.platform.modules.solicitud;

import com.jayway.jsonpath.JsonPath;
import gs.unmsm.platform.common.security.JwtService;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.CategoriaRepository;
import gs.unmsm.platform.modules.solicitud.repository.EvidenciaRepository;
import gs.unmsm.platform.modules.solicitud.repository.PrioridadRepository;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.model.HistorialEstado;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.storage.evidencias-dir=target/test-evidencias")
@AutoConfigureMockMvc
@Transactional
class SolicitudControllerTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired PrioridadRepository prioridades;
    @Autowired SolicitudRepository solicitudes;
    @Autowired HistorialEstadoRepository historial;
    @Autowired EvidenciaRepository evidencias;

    private String tokenDe(String correo) {
        Usuario u = usuarios.findByCorreoInstitucional(correo).orElseThrow();
        Long areaId = u.getArea() == null ? null : u.getArea().getId();
        return "Bearer " + jwtService.emitir(
                new UsuarioAutenticado(u.getId(), u.getNombres(), u.getRol().getNombre(), areaId)).token();
    }

    private String json(String asunto, String descripcion) {
        long categoria = categorias.findByNombre("Soporte informático").orElseThrow().getId();
        long prioridad = prioridades.findByNombre("Media").orElseThrow().getId();
        return """
                {"categoriaId":%d,"prioridadId":%d,"asunto":"%s","descripcion":"%s",
                 "pabellon":"Pabellón B","nivel":"2.º piso","ambiente":"Aula 204"}
                """.formatted(categoria, prioridad, asunto, descripcion);
    }

    private MockMultipartHttpServletRequestBuilder registro(String token, String cuerpo, MockMultipartFile... archivos) {
        MockMultipartHttpServletRequestBuilder req = multipart("/api/solicitudes");
        req.file(new MockMultipartFile("solicitud", "", "application/json", cuerpo.getBytes(StandardCharsets.UTF_8)));
        for (MockMultipartFile archivo : archivos) {
            req.file(archivo);
        }
        req.header("Authorization", token);
        return req;
    }

    @Test
    void estudianteRegistraSolicitudPendienteConCodigoPlazoYEvidencia() throws Exception {
        String token = tokenDe("andrea.mendoza@unmsm.edu.pe");
        MockMultipartFile foto = new MockMultipartFile("evidencias", "proyector.png", "image/png", PNG);

        String respuesta = mvc.perform(registro(token,
                        json("  El proyector no enciende  ", "El proyector del aula 204 no enciende desde ayer."), foto))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/solicitudes/SOL-")))
                .andExpect(jsonPath("$.codigo").value(org.hamcrest.Matchers.matchesPattern("SOL-\\d{4}-\\d{4}")))
                .andExpect(jsonPath("$.asunto").value("El proyector no enciende"))
                .andExpect(jsonPath("$.estado").value(EstadoNombre.PENDIENTE))
                .andExpect(jsonPath("$.area").value("Tecnologías de la información"))
                .andExpect(jsonPath("$.prioridad").value("Media"))
                .andExpect(jsonPath("$.evidencias").value(1))
                .andReturn().getResponse().getContentAsString();

        Solicitud guardada = solicitudes.findByCodigo(JsonPath.read(respuesta, "$.codigo")).orElseThrow();
        assertThat(guardada.getCodigo()).startsWith("SOL-" + guardada.getFechaCreacion().getYear() + "-");
        assertThat(guardada.getTecnico()).isNull();
        assertThat(guardada.getFechaVencimiento()).isAfter(guardada.getFechaCreacion().plusDays(6));

        List<HistorialEstado> eventos = historial.findBySolicitudIdOrderByFechaRegistroAscIdAsc(guardada.getId());
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).getEstadoOrigen()).isNull();
        assertThat(eventos.get(0).getEstadoDestino().getNombre()).isEqualTo(EstadoNombre.PENDIENTE);
        assertThat(eventos.get(0).getRolUsuario()).isEqualTo("ESTUDIANTE");

        var adjuntos = evidencias.findBySolicitudIdOrderByIdAsc(guardada.getId());
        assertThat(adjuntos).hasSize(1);
        assertThat(adjuntos.get(0).getNombreArchivo()).isEqualTo("proyector.png");
        assertThat(adjuntos.get(0).getTipoMime()).isEqualTo("image/png");
        assertThat(Files.exists(Path.of("target/test-evidencias").resolve(adjuntos.get(0).getUri()))).isTrue();
    }

    @Test
    void codigosSonCorrelativosDentroDelAnio() throws Exception {
        String token = tokenDe("luis.paredes@unmsm.edu.pe");
        String cuerpo = json("Fuga de agua en el baño", "Hay una fuga de agua en el baño del segundo piso.");
        String primero = JsonPath.read(mvc.perform(registro(token, cuerpo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.codigo");
        String segundo = JsonPath.read(mvc.perform(registro(token, cuerpo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.codigo");

        String prefijo = primero.substring(0, "SOL-AAAA-".length());
        int numero = Integer.parseInt(primero.substring(prefijo.length()));
        assertThat(segundo).isEqualTo(prefijo + "%04d".formatted(numero + 1));
    }

    @Test
    void camposInvalidosDevuelvenErroresPorCampo() throws Exception {
        String cuerpo = """
                {"categoriaId":null,"prioridadId":null,"asunto":"   ","descripcion":"Muy corta",
                 "pabellon":"","nivel":"","ambiente":""}
                """;
        mvc.perform(registro(tokenDe("andrea.mendoza@unmsm.edu.pe"), cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.categoriaId").value("Selecciona una categoría."))
                .andExpect(jsonPath("$.fieldErrors.prioridadId").value("Selecciona una prioridad."))
                .andExpect(jsonPath("$.fieldErrors.asunto").value("Escribe el asunto de la solicitud."))
                .andExpect(jsonPath("$.fieldErrors.descripcion")
                        .value("La descripción debe tener entre 20 y 2000 caracteres."))
                .andExpect(jsonPath("$.fieldErrors.pabellon").value("Indica el pabellón."));
    }

    @Test
    void archivoConExtensionFalsaEsRechazado() throws Exception {
        MockMultipartFile falso = new MockMultipartFile("evidencias", "foto.png", "image/png",
                "no soy una imagen".getBytes(StandardCharsets.UTF_8));
        mvc.perform(registro(tokenDe("andrea.mendoza@unmsm.edu.pe"),
                        json("Silla rota", "La silla del aula 101 tiene una pata rota."), falso))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.evidencias").value("“foto.png” no es PNG, JPG ni PDF."));
    }

    @Test
    void soloEstudiantesPuedenRegistrar() throws Exception {
        mvc.perform(registro(tokenDe("carlos.quispe@unmsm.edu.pe"),
                        json("Silla rota", "La silla del aula 101 tiene una pata rota.")))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/solicitudes")).andExpect(status().isUnauthorized());
    }

    @Test
    void catalogosParaElFormulario() throws Exception {
        String token = tokenDe("andrea.mendoza@unmsm.edu.pe");
        mvc.perform(get("/api/catalogos/categorias").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[?(@.nombre == 'Soporte informático')].area")
                        .value(contains("Tecnologías de la información")));
        mvc.perform(get("/api/catalogos/prioridades").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre").value(contains("Baja", "Media", "Alta", "Urgente")));
    }
}
