package gs.unmsm.platform.modules.auth;

import com.jayway.jsonpath.JsonPath;
import gs.unmsm.platform.common.security.JwtService;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    private static final String PASSWORD = "SanMarcos2026";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;

    private String loginBody(String correo, String password) {
        return "{\"correo\":\"%s\",\"password\":\"%s\"}".formatted(correo, password);
    }

    @Test
    void loginExitosoDevuelveTokenConClaims() throws Exception {
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("  Carlos.Quispe@unmsm.edu.pe ", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.rol").value("TECNICO"))
                .andExpect(jsonPath("$.usuario.areaNombre").value("Tecnologías de la información"))
                .andReturn().getResponse().getContentAsString();

        UsuarioAutenticado claims = jwtService.validar(JsonPath.read(json, "$.token")).orElseThrow();
        assertThat(claims.nombres()).isEqualTo("Carlos Quispe");
        assertThat(claims.rol()).isEqualTo("TECNICO");
        assertThat(claims.areaId()).isNotNull();
    }

    @Test
    void passwordIncorrectaYCorreoInexistenteDevuelvenElMismoError() throws Exception {
        String esperado = "Correo o contraseña incorrectos. Verifica tus datos e inténtalo de nuevo.";
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("andrea.mendoza@unmsm.edu.pe", "otra-clave")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(esperado));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("no.existe@unmsm.edu.pe", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(esperado));
    }

    @Test
    void cuentaInactivaEsRechazada() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("jorge.vega@unmsm.edu.pe", PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("inactiva")));
    }

    @Test
    void correoFueraDelDominioEsInvalido() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("andrea@gmail.com", PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correo").value("Usa tu correo institucional @unmsm.edu.pe."));
    }

    @Test
    void meRequiereTokenValido() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer token.invalido.x"))
                .andExpect(status().isUnauthorized());

        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("andrea.mendoza@unmsm.edu.pe", PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Andrea Mendoza"))
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.areaId").doesNotExist());
    }
}
