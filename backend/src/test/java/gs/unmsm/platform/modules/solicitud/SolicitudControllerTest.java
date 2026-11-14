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
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        return json("Soporte informático", asunto, descripcion, true);
    }

    private String json(String categoriaNombre, String asunto, String descripcion, boolean conUbicacion) {
        long categoria = categorias.findByNombre(categoriaNombre).orElseThrow().getId();
        long prioridad = prioridades.findByNombre("Media").orElseThrow().getId();
        String ubicacion = conUbicacion
                ? "\"pabellon\":\"Pabellón B\",\"nivel\":\"2.º piso\",\"ambiente\":\"Aula 204\""
                : "\"pabellon\":\" \",\"nivel\":null,\"ambiente\":\"\"";
        return """
                {"categoriaId":%d,"prioridadId":%d,"asunto":"%s","descripcion":"%s",%s}
                """.formatted(categoria, prioridad, asunto, descripcion, ubicacion);
    }

    private String registrarComo(String token, String cuerpo, MockMultipartFile... archivos) throws Exception {
        return JsonPath.read(mvc.perform(registro(token, cuerpo, archivos)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.codigo");
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
                .andExpect(jsonPath("$.fieldErrors.pabellon").doesNotExist());
    }

    @Test
    void laUbicacionEsOpcionalEnCualquierCategoria() throws Exception {
        String token = tokenDe("andrea.mendoza@unmsm.edu.pe");
        String codigo = registrarComo(token, json("Mantenimiento", "Fluorescente parpadea",
                "El fluorescente del pasillo parpadea todo el día.", false));
        Solicitud guardada = solicitudes.findByCodigo(codigo).orElseThrow();
        assertThat(guardada.getPabellon()).isNull();
        assertThat(guardada.getNivel()).isNull();
        assertThat(guardada.getAmbiente()).isNull();
    }

    @Test
    void estudianteVeSusSolicitudesYSuDetalleConHistorialYEvidencias() throws Exception {
        String andrea = tokenDe("andrea.mendoza@unmsm.edu.pe");
        String codigo = registrarComo(andrea, json("Proyector sin señal", "El proyector del aula 204 no muestra señal."),
                new MockMultipartFile("evidencias", "señal.png", "image/png", PNG));

        mvc.perform(get("/api/solicitudes/mias").header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(codigo))
                .andExpect(jsonPath("$[0].tecnico").doesNotExist())
                .andExpect(jsonPath("$[0].ultimaActualizacion").exists());

        String detalle = mvc.perform(get("/api/solicitudes/" + codigo).header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("El proyector del aula 204 no muestra señal."))
                .andExpect(jsonPath("$.solicitante").value("Andrea Mendoza"))
                .andExpect(jsonPath("$.historial", hasSize(1)))
                .andExpect(jsonPath("$.historial[0].estadoDestino").value(EstadoNombre.PENDIENTE))
                .andExpect(jsonPath("$.historial[0].rol").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.evidencias[0].nombre").value("señal.png"))
                .andReturn().getResponse().getContentAsString();

        Integer evidenciaId = JsonPath.read(detalle, "$.evidencias[0].id");
        mvc.perform(get("/api/solicitudes/%s/evidencias/%d".formatted(codigo, evidenciaId))
                        .header("Authorization", andrea))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(PNG));
    }

    @Test
    void solicitudAjenaResponde404SegunElRol() throws Exception {
        String codigo = registrarComo(tokenDe("andrea.mendoza@unmsm.edu.pe"),
                json("Proyector sin señal", "El proyector del aula 204 no muestra señal."));

        mvc.perform(get("/api/solicitudes/" + codigo).header("Authorization", tokenDe("luis.paredes@unmsm.edu.pe")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No encontramos la solicitud o no tienes acceso a ella."));
        mvc.perform(get("/api/solicitudes/" + codigo).header("Authorization", tokenDe("carlos.quispe@unmsm.edu.pe")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/solicitudes/" + codigo).header("Authorization", tokenDe("lucia.ramos@unmsm.edu.pe")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/solicitudes/" + codigo).header("Authorization", tokenDe("marcos.vidal@unmsm.edu.pe")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/solicitudes/SOL-1999-0001").header("Authorization", tokenDe("marcos.vidal@unmsm.edu.pe")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/solicitudes/mias").header("Authorization", tokenDe("carlos.quispe@unmsm.edu.pe")))
                .andExpect(status().isForbidden());
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
                        .value(contains("Tecnologías de la información")))
                .andExpect(jsonPath("$[?(@.requiereUbicacion == true)]").isEmpty());
        mvc.perform(get("/api/catalogos/prioridades").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre").value(contains("Baja", "Media", "Alta", "Urgente")));
    }

    @Test
    void elFlujoAvanzaEnOrdenYRechazaSaltos() throws Exception {
        String andrea = tokenDe("andrea.mendoza@unmsm.edu.pe");
        String ronald = tokenDe("ronald.munayco@unmsm.edu.pe");
        String diana = tokenDe("diana.cardenas@unmsm.edu.pe");
        long dianaId = usuarios.findByCorreoInstitucional("diana.cardenas@unmsm.edu.pe").orElseThrow().getId();
        long carlosId = usuarios.findByCorreoInstitucional("carlos.quispe@unmsm.edu.pe").orElseThrow().getId();
        String codigo = registrarComo(andrea, json("Mantenimiento", "Fuga en el lavadero",
                "El lavadero del primer piso pierde agua desde esta mañana.", true));

        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", ronald)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Asignado\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No puedes pasar esta solicitud de Pendiente a Asignado."));
        mvc.perform(get("/api/solicitudes/" + codigo).header("Authorization", ronald))
                .andExpect(jsonPath("$.estado").value("Pendiente"))
                .andExpect(jsonPath("$.accion.estado").value("En revisión"))
                .andExpect(jsonPath("$.accion.requiereTecnico").value(false));

        avanzar(ronald, codigo, "{\"estado\":\"En revisión\"}");
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", ronald)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Asignado\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.tecnicoId").value("Elige al técnico que la atenderá."));
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", ronald)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"Asignado\",\"tecnicoId\":" + carlosId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.tecnicoId").value("Elige un técnico activo de esta área."));

        avanzar(ronald, codigo, "{\"estado\":\"Asignado\",\"tecnicoId\":" + dianaId + "}");
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", andrea)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Cerrado\"}"))
                .andExpect(status().isConflict());

        avanzar(diana, codigo, "{\"estado\":\"En proceso\",\"nota\":\"Ya revisé la llave.\"}");
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", diana)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Resuelto\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nota").value("Escribe una nota técnica al marcar como resuelta."));
        avanzar(diana, codigo, "{\"estado\":\"Resuelto\",\"nota\":\"Se cambió la llave del lavadero.\"}");
        mvc.perform(get("/api/solicitudes/" + codigo).header("Authorization", ronald))
                .andExpect(jsonPath("$.estado").value("Resuelto"))
                .andExpect(jsonPath("$.accion.estado").value("Cerrado"))
                .andExpect(jsonPath("$.accion.requiereNota").value(false));
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", andrea)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Cerrado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Cerrado"))
                .andExpect(jsonPath("$.tecnico").value("Diana Cárdenas"))
                .andExpect(jsonPath("$.accion").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.historial", hasSize(6)))
                .andExpect(jsonPath("$.historial[3].nota").value("Ya revisé la llave."))
                .andExpect(jsonPath("$.historial[3].estadoOrigen").value("Asignado"))
                .andExpect(jsonPath("$.historial[5].estadoDestino").value("Cerrado"));

        mvc.perform(get("/api/solicitudes/area").header("Authorization", ronald))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == '" + codigo + "')].estado").value(contains("Cerrado")));
        mvc.perform(get("/api/solicitudes/asignadas").header("Authorization", diana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == '" + codigo + "')].tecnico").value(contains("Diana Cárdenas")));
        mvc.perform(get("/api/solicitudes/tecnicos").header("Authorization", ronald))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombres == 'Diana Cárdenas')]").exists());

        mvc.perform(get("/api/solicitudes/" + codigo).header("Authorization", tokenDe("lucia.ramos@unmsm.edu.pe")))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", tokenDe("marcos.vidal@unmsm.edu.pe"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"En revisión\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/solicitudes/todas").header("Authorization", tokenDe("marcos.vidal@unmsm.edu.pe")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == '" + codigo + "')].estado").value(contains("Cerrado")));
        mvc.perform(get("/api/solicitudes/todas").header("Authorization", andrea)).andExpect(status().isForbidden());
        mvc.perform(get("/api/solicitudes/area").header("Authorization", andrea)).andExpect(status().isForbidden());
        mvc.perform(get("/api/solicitudes/asignadas").header("Authorization", ronald)).andExpect(status().isForbidden());
    }

    private void avanzar(String token, String codigo, String cuerpo) throws Exception {
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk());
    }
}
