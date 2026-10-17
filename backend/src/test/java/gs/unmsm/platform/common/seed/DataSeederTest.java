package gs.unmsm.platform.common.seed;

import gs.unmsm.platform.modules.solicitud.repository.CategoriaRepository;
import gs.unmsm.platform.modules.solicitud.repository.EstadoRepository;
import gs.unmsm.platform.modules.solicitud.repository.PrioridadRepository;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.AreaRepository;
import gs.unmsm.platform.modules.usuario.repository.RolRepository;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataSeederTest {

    @Autowired RolRepository rolRepository;
    @Autowired AreaRepository areaRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired PrioridadRepository prioridadRepository;
    @Autowired EstadoRepository estadoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void cargaLosCatalogosBase() {
        assertThat(rolRepository.count()).isEqualTo(4);
        assertThat(areaRepository.count()).isEqualTo(5);
        assertThat(categoriaRepository.findByNombre("Soporte informático"))
                .hasValueSatisfying(c -> assertThat(c.getArea().getNombre()).isEqualTo("Tecnologías de la información"));
        assertThat(prioridadRepository.findByNombre("Urgente"))
                .hasValueSatisfying(p -> {
                    assertThat(p.getSlaDias()).isEqualTo(1);
                    assertThat(p.getDiasHabiles()).isFalse();
                });
        assertThat(estadoRepository.findByNombre("Cerrado"))
                .hasValueSatisfying(e -> assertThat(e.getOrden()).isEqualTo(6));
        assertThat(areaRepository.findByNombre("Tecnologías de la información"))
                .hasValueSatisfying(a -> assertThat(a.getResponsable().getNombres()).isEqualTo("Carlos Quispe"));
    }

    @Test
    void creaUsuariosDemoConCorreoInstitucional() {
        assertThat(usuarioRepository.count()).isGreaterThanOrEqualTo(18);
        assertThat(usuarioRepository.findByCorreoInstitucional("max.astuhuaman@unmsm.edu.pe"))
                .hasValueSatisfying(u -> {
                    assertThat(u.getRol().getNombre()).isEqualTo(RolNombre.ESTUDIANTE);
                    assertThat(passwordEncoder.matches("unmsm", u.getPasswordHash())).isTrue();
                });
        assertThat(usuarioRepository.findByCorreoInstitucional("favio.rouillon@unmsm.edu.pe"))
                .hasValueSatisfying(u -> assertThat(u.getRol().getNombre()).isEqualTo(RolNombre.TECNICO));
        assertThat(usuarioRepository.findByCorreoInstitucional("ronald.munayco@unmsm.edu.pe"))
                .hasValueSatisfying(u -> assertThat(u.getRol().getNombre()).isEqualTo(RolNombre.SUPERVISOR));
        assertThat(usuarioRepository.findByCorreoInstitucional("diego.guzman@unmsm.edu.pe"))
                .hasValueSatisfying(u -> assertThat(u.getRol().getNombre()).isEqualTo(RolNombre.ADMINISTRADOR));
        assertThat(usuarioRepository.findByCorreoInstitucional("kenner.tataje@unmsm.edu.pe"))
                .hasValueSatisfying(u -> assertThat(u.getNombres()).isEqualTo("Kenner Tataje"));
        assertThat(usuarioRepository.findByCorreoInstitucional("kenner.tataja@unmsm.edu.pe")).isEmpty();

        Usuario tecnica = usuarioRepository.findByCorreoInstitucional("diana.cardenas@unmsm.edu.pe").orElseThrow();
        assertThat(tecnica.getRol().getNombre()).isEqualTo(RolNombre.TECNICO);
        assertThat(tecnica.getArea().getNombre()).isEqualTo("Mantenimiento general");
        assertThat(passwordEncoder.matches("SanMarcos2026", tecnica.getPasswordHash())).isTrue();

        assertThat(usuarioRepository.findByCorreoInstitucional("jorge.vega@unmsm.edu.pe"))
                .hasValueSatisfying(u -> assertThat(u.getActivo()).isFalse());
    }

    @Test
    void normalizaElCorreo() {
        assertThat(DataSeeder.correoInstitucional("Lucía  Ramos")).isEqualTo("lucia.ramos@unmsm.edu.pe");
    }
}
