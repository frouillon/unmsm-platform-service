package gs.unmsm.platform.common.seed;

import gs.unmsm.platform.modules.solicitud.model.Categoria;
import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Prioridad;
import gs.unmsm.platform.modules.solicitud.repository.CategoriaRepository;
import gs.unmsm.platform.modules.solicitud.repository.EstadoRepository;
import gs.unmsm.platform.modules.solicitud.repository.PrioridadRepository;
import gs.unmsm.platform.modules.usuario.model.Area;
import gs.unmsm.platform.modules.usuario.model.Rol;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.AreaRepository;
import gs.unmsm.platform.modules.usuario.repository.RolRepository;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Carga los catálogos base y los usuarios de demostración del prototipo.
 * Solo inserta lo que no existe, por lo que es seguro ejecutarlo en cada arranque.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final String AREA_MANTENIMIENTO = "Mantenimiento general";
    private static final String AREA_INFRAESTRUCTURA = "Infraestructura y obras";
    private static final String AREA_TI = "Tecnologías de la información";
    private static final String AREA_SECRETARIA = "Secretaría académica";
    private static final String AREA_SERVICIOS = "Servicios generales";

    private final RolRepository rolRepository;
    private final AreaRepository areaRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrioridadRepository prioridadRepository;
    private final EstadoRepository estadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.demo-password}")
    private String demoPassword;

    private record CategoriaSeed(String nombre, String area) {}
    private record PrioridadSeed(String nombre, int slaDias, boolean diasHabiles) {}
    private record UsuarioSeed(String nombres, String rol, String area, boolean activo) {}
    private record ResponsableSeed(String area, String nombres) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List.of(RolNombre.ESTUDIANTE, RolNombre.TECNICO, RolNombre.SUPERVISOR, RolNombre.ADMINISTRADOR)
                .forEach(this::rol);

        List.of(AREA_MANTENIMIENTO, AREA_INFRAESTRUCTURA, AREA_TI, AREA_SECRETARIA, AREA_SERVICIOS)
                .forEach(this::area);

        List.of(
                new CategoriaSeed("Mantenimiento", AREA_MANTENIMIENTO),
                new CategoriaSeed("Infraestructura", AREA_INFRAESTRUCTURA),
                new CategoriaSeed("Soporte informático", AREA_TI),
                new CategoriaSeed("Administrativa", AREA_SECRETARIA),
                new CategoriaSeed("Reserva de ambientes", AREA_SERVICIOS)
        ).forEach(this::categoria);

        List.of(
                new PrioridadSeed("Baja", 14, true),
                new PrioridadSeed("Media", 7, true),
                new PrioridadSeed("Alta", 3, true),
                new PrioridadSeed("Urgente", 1, false)
        ).forEach(this::prioridad);

        List<String> estados = List.of(EstadoNombre.PENDIENTE, EstadoNombre.EN_REVISION, EstadoNombre.ASIGNADO,
                EstadoNombre.EN_PROCESO, EstadoNombre.RESUELTO, EstadoNombre.CERRADO);
        for (int i = 0; i < estados.size(); i++) {
            estado(estados.get(i), i + 1);
        }

        List.of(
                new UsuarioSeed("Andrea Mendoza", RolNombre.ESTUDIANTE, null, true),
                new UsuarioSeed("Luis Paredes", RolNombre.ESTUDIANTE, null, true),
                new UsuarioSeed("Camila Rojas", RolNombre.ESTUDIANTE, null, true),
                new UsuarioSeed("Sofía Díaz", RolNombre.ESTUDIANTE, null, true),
                new UsuarioSeed("Jorge Vega", RolNombre.ESTUDIANTE, null, false),
                new UsuarioSeed("Carlos Quispe", RolNombre.TECNICO, AREA_TI, true),
                new UsuarioSeed("Mario Torres", RolNombre.TECNICO, AREA_INFRAESTRUCTURA, true),
                new UsuarioSeed("Diana Cárdenas", RolNombre.TECNICO, AREA_MANTENIMIENTO, true),
                new UsuarioSeed("Rosa Salinas", RolNombre.TECNICO, AREA_SECRETARIA, true),
                new UsuarioSeed("Pedro Salas", RolNombre.TECNICO, AREA_SERVICIOS, true),
                new UsuarioSeed("Lucía Ramos", RolNombre.SUPERVISOR, AREA_SERVICIOS, true),
                new UsuarioSeed("Marcos Vidal", RolNombre.ADMINISTRADOR, AREA_TI, true)
        ).forEach(this::usuario);

        List.of(
                new ResponsableSeed(AREA_MANTENIMIENTO, "Diana Cárdenas"),
                new ResponsableSeed(AREA_INFRAESTRUCTURA, "Mario Torres"),
                new ResponsableSeed(AREA_TI, "Carlos Quispe"),
                new ResponsableSeed(AREA_SECRETARIA, "Rosa Salinas"),
                new ResponsableSeed(AREA_SERVICIOS, "Pedro Salas")
        ).forEach(this::responsable);

        log.info("Datos semilla verificados ({} usuarios).", usuarioRepository.count());
    }

    private Rol rol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(nombre).build()));
    }

    private Area area(String nombre) {
        return areaRepository.findByNombre(nombre)
                .orElseGet(() -> areaRepository.save(Area.builder().nombre(nombre).build()));
    }

    private void categoria(CategoriaSeed seed) {
        if (categoriaRepository.findByNombre(seed.nombre()).isEmpty()) {
            categoriaRepository.save(Categoria.builder().nombre(seed.nombre()).area(area(seed.area())).build());
        }
    }

    private void prioridad(PrioridadSeed seed) {
        if (prioridadRepository.findByNombre(seed.nombre()).isEmpty()) {
            prioridadRepository.save(Prioridad.builder()
                    .nombre(seed.nombre()).slaDias(seed.slaDias()).diasHabiles(seed.diasHabiles()).build());
        }
    }

    private void estado(String nombre, int orden) {
        if (estadoRepository.findByNombre(nombre).isEmpty()) {
            estadoRepository.save(Estado.builder().nombre(nombre).orden(orden).build());
        }
    }

    private void usuario(UsuarioSeed seed) {
        String correo = correoInstitucional(seed.nombres());
        if (usuarioRepository.findByCorreoInstitucional(correo).isPresent()) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .nombres(seed.nombres())
                .correoInstitucional(correo)
                .passwordHash(passwordEncoder.encode(demoPassword))
                .rol(rol(seed.rol()))
                .area(seed.area() == null ? null : area(seed.area()))
                .activo(seed.activo())
                .build());
    }

    private void responsable(ResponsableSeed seed) {
        Area area = area(seed.area());
        if (area.getResponsable() == null) {
            usuarioRepository.findByCorreoInstitucional(correoInstitucional(seed.nombres()))
                    .ifPresent(area::setResponsable);
        }
    }

    static String correoInstitucional(String nombres) {
        String sinTildes = Normalizer.normalize(nombres, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", ".") + "@unmsm.edu.pe";
    }
}
