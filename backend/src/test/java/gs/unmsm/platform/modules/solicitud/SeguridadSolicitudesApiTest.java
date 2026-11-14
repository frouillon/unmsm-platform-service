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
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-02 · HU-08 · HU-11 — Seguridad de la API vista desde fuera: peticiones HTTP reales
 * contra la aplicación completa y la base de datos.
 * <p>
 * Técnica: conjetura de errores. Cada prueba intenta un abuso concreto y, junto a él,
 * comprueba el caso legítimo, para confirmar que el rechazo se debe a la regla y no a otra causa.
 */
@SpringBootTest(properties = "app.storage.evidencias-dir=target/test-evidencias")
@AutoConfigureMockMvc
@Transactional
class SeguridadSolicitudesApiTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};

    private static final String ANDREA = "andrea.mendoza@unmsm.edu.pe";
    private static final String LUIS = "luis.paredes@unmsm.edu.pe";
    private static final String ADMINISTRADOR = "marcos.vidal@unmsm.edu.pe";
    private static final String CARLOS_TECNICO_TI = "carlos.quispe@unmsm.edu.pe";
    private static final String KENNER_TECNICO_TI = "kenner.tataje@unmsm.edu.pe";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired PrioridadRepository prioridades;
    @Autowired SolicitudRepository solicitudes;
    @Autowired EvidenciaRepository evidencias;

    @Value("${app.jwt.secret}")
    String secretoJwt;

    @Test
    void unEstudianteNoDescargaLaEvidenciaDeOtroUsandoSuPropiaSolicitud() throws Exception {
        String andrea = tokenDe(ANDREA);
        String luis = tokenDe(LUIS);
        String solicitudDeAndrea = registrarConEvidencia(andrea, "Proyector sin señal");
        String solicitudDeLuis = registrarConEvidencia(luis, "Teclado sin respuesta");
        long evidenciaDeAndrea = evidenciaDe(solicitudDeAndrea);
        long evidenciaDeLuis = evidenciaDe(solicitudDeLuis);

        mvc.perform(get(rutaDeEvidencia(solicitudDeLuis, evidenciaDeLuis)).header("Authorization", luis))
                .andExpect(status().isOk());

        mvc.perform(get(rutaDeEvidencia(solicitudDeLuis, evidenciaDeAndrea)).header("Authorization", luis))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("La evidencia no existe."));
    }

    @Test
    void unTecnicoNoPuedeIniciarLaAtencionDeUnaSolicitudAsignadaAOtro() throws Exception {
        String administrador = tokenDe(ADMINISTRADOR);
        String codigo = registrar(tokenDe(ANDREA), "Computadora no enciende");
        avanzar(administrador, codigo, "{\"estado\":\"En revisión\"}");
        avanzar(administrador, codigo, "{\"estado\":\"Asignado\",\"tecnicoId\":" + idDe(CARLOS_TECNICO_TI) + "}");

        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", tokenDe(KENNER_TECNICO_TI))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"En proceso\"}"))
                .andExpect(status().isNotFound());
        assertThat(estadoDe(codigo)).isEqualTo(EstadoNombre.ASIGNADO);

        avanzar(tokenDe(CARLOS_TECNICO_TI), codigo, "{\"estado\":\"En proceso\"}");
        assertThat(estadoDe(codigo)).isEqualTo(EstadoNombre.EN_PROCESO);
    }

    @Test
    void unTokenVencidoNoDaAccesoALaApi() throws Exception {
        Usuario andrea = usuarios.findByCorreoInstitucional(ANDREA).orElseThrow();
        UsuarioAutenticado sesion =
                new UsuarioAutenticado(andrea.getId(), andrea.getNombres(), andrea.getRol().getNombre(), null);
        JwtService emisorDeTokensVencidos = new JwtService(secretoJwt, -1);
        String tokenVencido = "Bearer " + emisorDeTokensVencidos.emitir(sesion).token();
        String tokenVigente = "Bearer " + jwtService.emitir(sesion).token();

        mvc.perform(get("/api/solicitudes/mias").header("Authorization", tokenVencido))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/solicitudes/mias").header("Authorization", tokenVigente))
                .andExpect(status().isOk());
    }

    // --- Apoyo -------------------------------------------------------------------------------

    private String tokenDe(String correo) {
        Usuario u = usuarios.findByCorreoInstitucional(correo).orElseThrow();
        Long areaId = u.getArea() == null ? null : u.getArea().getId();
        return "Bearer " + jwtService.emitir(
                new UsuarioAutenticado(u.getId(), u.getNombres(), u.getRol().getNombre(), areaId)).token();
    }

    private long idDe(String correo) {
        return usuarios.findByCorreoInstitucional(correo).orElseThrow().getId();
    }

    /** Registra una solicitud de Soporte informático (área de TI) y devuelve su código. */
    private String registrar(String token, String asunto, MockMultipartFile... archivos) throws Exception {
        long categoria = categorias.findByNombre("Soporte informático").orElseThrow().getId();
        long prioridad = prioridades.findByNombre("Media").orElseThrow().getId();
        String cuerpo = """
                {"categoriaId":%d,"prioridadId":%d,"asunto":"%s","descripcion":"Descripción de prueba con más de veinte caracteres."}
                """.formatted(categoria, prioridad, asunto);

        MockMultipartHttpServletRequestBuilder peticion = multipart("/api/solicitudes");
        peticion.file(new MockMultipartFile("solicitud", "", "application/json", cuerpo.getBytes(StandardCharsets.UTF_8)));
        for (MockMultipartFile archivo : archivos) {
            peticion.file(archivo);
        }
        peticion.header("Authorization", token);

        return JsonPath.read(mvc.perform(peticion).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.codigo");
    }

    private String registrarConEvidencia(String token, String asunto) throws Exception {
        return registrar(token, asunto, new MockMultipartFile("evidencias", "foto.png", "image/png", PNG));
    }

    /** Id de la única evidencia adjunta a la solicitud. */
    private long evidenciaDe(String codigo) {
        Solicitud solicitud = solicitudes.findByCodigo(codigo).orElseThrow();
        return evidencias.findBySolicitudIdOrderByIdAsc(solicitud.getId()).get(0).getId();
    }

    private String estadoDe(String codigo) {
        return solicitudes.findByCodigo(codigo).orElseThrow().getEstado().getNombre();
    }

    private static String rutaDeEvidencia(String codigo, long evidenciaId) {
        return "/api/solicitudes/" + codigo + "/evidencias/" + evidenciaId;
    }

    private void avanzar(String token, String codigo, String cuerpo) throws Exception {
        mvc.perform(post("/api/solicitudes/" + codigo + "/estado").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk());
    }
}
