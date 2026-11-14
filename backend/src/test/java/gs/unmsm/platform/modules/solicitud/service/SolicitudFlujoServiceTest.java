package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.common.exception.BusinessRuleException;
import gs.unmsm.platform.common.exception.FieldValidationException;
import gs.unmsm.platform.common.exception.UnauthorizedException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.solicitud.dto.AvanzarSolicitudRequest;
import gs.unmsm.platform.modules.solicitud.model.Estado;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.EstadoRepository;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.model.HistorialEstado;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository;
import gs.unmsm.platform.modules.usuario.model.Area;
import gs.unmsm.platform.modules.usuario.model.Rol;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.model.Usuario;
import gs.unmsm.platform.modules.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-09 · HU-10 · HU-17 — Avance de una solicitud por su flujo de atención.
 * <p>
 * Técnica: caja blanca, cobertura de ramas de {@link SolicitudFlujoService#avanzar}: cada prueba
 * recorre una salida distinta del método. Los repositorios se sustituyen con Mockito, de modo que
 * se prueba solo la lógica del servicio, sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class SolicitudFlujoServiceTest {

    private static final String CODIGO = "SOL-2026-0001";
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 20, 10, 30);

    @Mock
    SolicitudRepository solicitudes;
    @Mock
    EstadoRepository estados;
    @Mock
    UsuarioRepository usuarios;
    @Mock
    HistorialEstadoRepository historial;

    private SolicitudFlujoService servicio;

    private final Area mantenimiento = Area.builder().id(1L).nombre("Mantenimiento").build();
    private final Area tecnologia = Area.builder().id(2L).nombre("Tecnologías de la información").build();

    private final Usuario andrea = usuario(10L, RolNombre.ESTUDIANTE, null, true);
    private final Usuario supervisor = usuario(20L, RolNombre.SUPERVISOR, mantenimiento, true);
    private final UsuarioAutenticado sesionDelSupervisor =
            new UsuarioAutenticado(20L, "Ronald Munayco", RolNombre.SUPERVISOR, mantenimiento.getId());

    @BeforeEach
    void crearServicio() {
        Clock relojFijo = Clock.fixed(AHORA.atZone(LIMA).toInstant(), LIMA);
        servicio = new SolicitudFlujoService(solicitudes, estados, usuarios, historial, relojFijo);
    }

    @Test
    void unaTransicionFueraDelFlujoSeRechazaYNoQuedaEnElHistorial() {
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.PENDIENTE);
        when(solicitudes.findByCodigo(CODIGO)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> servicio.avanzar(sesionDelSupervisor, CODIGO, pasarA(EstadoNombre.ASIGNADO)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No puedes pasar esta solicitud de Pendiente a Asignado.");

        assertThat(solicitud.getEstado().getNombre()).isEqualTo(EstadoNombre.PENDIENTE);
        verify(historial, never()).save(any(HistorialEstado.class));
    }

    @Test
    void noSePuedeAsignarUnTecnicoDeOtraArea() {
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.EN_REVISION);
        Usuario tecnicoDeOtraArea = usuario(30L, RolNombre.TECNICO, tecnologia, true);
        cuandoElSupervisorAbre(solicitud);
        when(usuarios.findById(30L)).thenReturn(Optional.of(tecnicoDeOtraArea));

        assertThatThrownBy(() -> servicio.avanzar(sesionDelSupervisor, CODIGO, asignarA(30L)))
                .isInstanceOf(FieldValidationException.class)
                .hasMessage("Elige un técnico activo de esta área.");

        assertQueNoSeAsigno(solicitud);
    }

    @Test
    void noSePuedeAsignarUnTecnicoInactivo() {
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.EN_REVISION);
        Usuario tecnicoInactivo = usuario(31L, RolNombre.TECNICO, mantenimiento, false);
        cuandoElSupervisorAbre(solicitud);
        when(usuarios.findById(31L)).thenReturn(Optional.of(tecnicoInactivo));

        assertThatThrownBy(() -> servicio.avanzar(sesionDelSupervisor, CODIGO, asignarA(31L)))
                .isInstanceOf(FieldValidationException.class)
                .hasMessage("Elige un técnico activo de esta área.");

        assertQueNoSeAsigno(solicitud);
    }

    @Test
    void noSePuedeAsignarAUnUsuarioQueNoEsTecnico() {
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.EN_REVISION);
        Usuario otroSupervisor = usuario(32L, RolNombre.SUPERVISOR, mantenimiento, true);
        cuandoElSupervisorAbre(solicitud);
        when(usuarios.findById(32L)).thenReturn(Optional.of(otroSupervisor));

        assertThatThrownBy(() -> servicio.avanzar(sesionDelSupervisor, CODIGO, asignarA(32L)))
                .isInstanceOf(FieldValidationException.class)
                .hasMessage("Elige un técnico activo de esta área.");

        assertQueNoSeAsigno(solicitud);
    }

    @Test
    void asignarSinElegirTecnicoEsUnErrorDelCampoTecnico() {
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.EN_REVISION);
        cuandoElSupervisorAbre(solicitud);

        assertThatThrownBy(() -> servicio.avanzar(sesionDelSupervisor, CODIGO, pasarA(EstadoNombre.ASIGNADO)))
                .isInstanceOf(FieldValidationException.class)
                .hasMessage("Elige al técnico que la atenderá.");

        assertQueNoSeAsigno(solicitud);
    }

    @Test
    void siLaCuentaDelTecnicoFueDesactivadaSuTokenYaNoLePermiteAvanzar() {
        Usuario tecnicoDesactivado = usuario(33L, RolNombre.TECNICO, mantenimiento, false);
        UsuarioAutenticado sesionDelTecnico =
                new UsuarioAutenticado(33L, "Diana Cárdenas", RolNombre.TECNICO, mantenimiento.getId());
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.ASIGNADO);
        solicitud.setTecnico(tecnicoDesactivado);
        when(solicitudes.findByCodigo(CODIGO)).thenReturn(Optional.of(solicitud));
        when(usuarios.findById(33L)).thenReturn(Optional.of(tecnicoDesactivado));

        assertThatThrownBy(() -> servicio.avanzar(sesionDelTecnico, CODIGO, pasarA(EstadoNombre.EN_PROCESO)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Tu sesión ya no es válida. Inicia sesión nuevamente.");

        assertThat(solicitud.getEstado().getNombre()).isEqualTo(EstadoNombre.ASIGNADO);
        verify(historial, never()).save(any(HistorialEstado.class));
    }

    @Test
    void unAvanceValidoCambiaElEstadoYLoRegistraEnElHistorial() {
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.PENDIENTE);
        Estado enRevision = estado(EstadoNombre.EN_REVISION);
        cuandoElSupervisorAbre(solicitud);
        when(estados.findByNombre(EstadoNombre.EN_REVISION)).thenReturn(Optional.of(enRevision));

        servicio.avanzar(sesionDelSupervisor, CODIGO,
                new AvanzarSolicitudRequest(EstadoNombre.EN_REVISION, null, "Se revisará mañana."));

        assertThat(solicitud.getEstado()).isSameAs(enRevision);

        ArgumentCaptor<HistorialEstado> guardado = ArgumentCaptor.forClass(HistorialEstado.class);
        verify(historial).save(guardado.capture());
        HistorialEstado movimiento = guardado.getValue();
        assertThat(movimiento.getSolicitud()).isSameAs(solicitud);
        assertThat(movimiento.getEstadoOrigen().getNombre()).isEqualTo(EstadoNombre.PENDIENTE);
        assertThat(movimiento.getEstadoDestino()).isSameAs(enRevision);
        assertThat(movimiento.getUsuario()).isSameAs(supervisor);
        assertThat(movimiento.getRolUsuario()).isEqualTo(RolNombre.SUPERVISOR);
        assertThat(movimiento.getNotaTecnica()).isEqualTo("Se revisará mañana.");
        assertThat(movimiento.getFechaRegistro()).isEqualTo(AHORA);
    }

    @Test
    void marcarComoResueltaSinNotaSeRechaza() {
        Usuario tecnico = usuario(33L, RolNombre.TECNICO, mantenimiento, true);
        UsuarioAutenticado sesionDelTecnico =
                new UsuarioAutenticado(33L, "Diana Cárdenas", RolNombre.TECNICO, mantenimiento.getId());
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.EN_PROCESO);
        solicitud.setTecnico(tecnico);
        when(solicitudes.findByCodigo(CODIGO)).thenReturn(Optional.of(solicitud));
        when(usuarios.findById(33L)).thenReturn(Optional.of(tecnico));

        assertThatThrownBy(() -> servicio.avanzar(sesionDelTecnico, CODIGO, pasarA(EstadoNombre.RESUELTO)))
                .isInstanceOf(FieldValidationException.class)
                .hasMessage("Escribe una nota técnica al marcar como resuelta.");

        assertThat(solicitud.getEstado().getNombre()).isEqualTo(EstadoNombre.EN_PROCESO);
        verify(historial, never()).save(any(HistorialEstado.class));
    }

    @Test
    void elSupervisorPuedeCerrarUnaSolicitudResuelta() {
        Solicitud solicitud = solicitudDeMantenimientoEn(EstadoNombre.RESUELTO);
        Estado cerrado = estado(EstadoNombre.CERRADO);
        cuandoElSupervisorAbre(solicitud);
        when(estados.findByNombre(EstadoNombre.CERRADO)).thenReturn(Optional.of(cerrado));

        servicio.avanzar(sesionDelSupervisor, CODIGO, pasarA(EstadoNombre.CERRADO));

        assertThat(solicitud.getEstado()).isSameAs(cerrado);
        verify(historial).save(any(HistorialEstado.class));
    }

    // --- Preparación de los escenarios -------------------------------------------------------

    /** El supervisor de Mantenimiento, con su cuenta activa, abre la solicitud indicada. */
    private void cuandoElSupervisorAbre(Solicitud solicitud) {
        when(solicitudes.findByCodigo(CODIGO)).thenReturn(Optional.of(solicitud));
        when(usuarios.findById(supervisor.getId())).thenReturn(Optional.of(supervisor));
    }

    private void assertQueNoSeAsigno(Solicitud solicitud) {
        assertThat(solicitud.getTecnico()).isNull();
        assertThat(solicitud.getEstado().getNombre()).isEqualTo(EstadoNombre.EN_REVISION);
        verify(historial, never()).save(any(HistorialEstado.class));
    }

    private Solicitud solicitudDeMantenimientoEn(String nombreDelEstado) {
        return Solicitud.builder()
                .id(100L)
                .codigo(CODIGO)
                .solicitante(andrea)
                .area(mantenimiento)
                .estado(estado(nombreDelEstado))
                .build();
    }

    private static AvanzarSolicitudRequest pasarA(String estado) {
        return new AvanzarSolicitudRequest(estado, null, null);
    }

    private static AvanzarSolicitudRequest asignarA(long tecnicoId) {
        return new AvanzarSolicitudRequest(EstadoNombre.ASIGNADO, tecnicoId, null);
    }

    private static Estado estado(String nombre) {
        return Estado.builder().nombre(nombre).build();
    }

    private static Usuario usuario(long id, String rol, Area area, boolean activo) {
        return Usuario.builder()
                .id(id)
                .rol(Rol.builder().nombre(rol).build())
                .area(area)
                .activo(activo)
                .build();
    }
}
