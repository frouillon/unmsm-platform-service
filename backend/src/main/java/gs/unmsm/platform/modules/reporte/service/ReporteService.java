package gs.unmsm.platform.modules.reporte.service;

import gs.unmsm.platform.common.exception.FieldValidationException;
import gs.unmsm.platform.common.exception.ForbiddenException;
import gs.unmsm.platform.common.exception.ResourceNotFoundException;
import gs.unmsm.platform.common.security.UsuarioAutenticado;
import gs.unmsm.platform.modules.dashboard.dto.ConteoItem;
import gs.unmsm.platform.modules.reporte.dto.AreaOpcion;
import gs.unmsm.platform.modules.reporte.dto.CargaTecnico;
import gs.unmsm.platform.modules.reporte.dto.FilaReporte;
import gs.unmsm.platform.modules.reporte.dto.ReporteSolicitudesResponse;
import gs.unmsm.platform.modules.reporte.dto.ResumenReporte;
import gs.unmsm.platform.modules.solicitud.model.EstadoNombre;
import gs.unmsm.platform.modules.solicitud.model.Solicitud;
import gs.unmsm.platform.modules.solicitud.repository.SolicitudRepository;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository;
import gs.unmsm.platform.modules.trazabilidad.repository.HistorialEstadoRepository.UltimoMovimiento;
import gs.unmsm.platform.modules.usuario.model.Area;
import gs.unmsm.platform.modules.usuario.model.RolNombre;
import gs.unmsm.platform.modules.usuario.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reportes operativos (HU-23 y HU-24): solicitudes registradas en un rango de fechas con su cumplimiento de SLA,
 * tiempo medio de resolución (MTTR) y matriz de carga técnica. El supervisor solo ve su área; el administrador
 * ve todas o filtra por una.
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    /** Rango máximo permitido, para acotar el costo de la consulta. */
    static final int MAX_DIAS = 366;
    /** Si no se indica rango, el reporte cubre los últimos 30 días. */
    static final int DIAS_POR_DEFECTO = 30;
    static final String TODAS_LAS_AREAS = "Todas las áreas";

    private static final String ATENDIDA_SIN_FECHA = "Atendida";
    private static final BigDecimal MINUTOS_POR_DIA = BigDecimal.valueOf(24 * 60);

    private final SolicitudRepository solicitudes;
    private final HistorialEstadoRepository historial;
    private final AreaRepository areas;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ReporteSolicitudesResponse generar(UsuarioAutenticado usuario, LocalDate desde, LocalDate hasta, Long areaId) {
        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        LocalDate fin = hasta != null ? hasta : ahora.toLocalDate();
        LocalDate inicio = desde != null ? desde : fin.minusDays(DIAS_POR_DEFECTO - 1L);
        validarRango(inicio, fin);

        Long area = alcance(usuario, areaId);
        String alcance = area == null ? TODAS_LAS_AREAS : areas.findById(area)
                .map(Area::getNombre)
                .orElseThrow(() -> new ResourceNotFoundException("No encontramos esa área."));

        List<Solicitud> lista = solicitudes.registradasEntre(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay(), area);
        Map<Long, LocalDateTime> resoluciones = lista.isEmpty() ? Map.of() : historial
                .fechasDeResolucion(lista.stream().map(Solicitud::getId).toList()).stream()
                .collect(Collectors.toMap(UltimoMovimiento::getSolicitudId, UltimoMovimiento::getFecha));

        List<FilaReporte> filas = lista.stream()
                .map(s -> fila(s, resoluciones.get(s.getId()), ahora))
                .toList();

        return new ReporteSolicitudesResponse(inicio, fin, alcance, ahora,
                resumen(lista, filas), cargaTecnica(filas), filas);
    }

    @Transactional(readOnly = true)
    public List<AreaOpcion> areasDisponibles(UsuarioAutenticado usuario) {
        Long propia = alcance(usuario, null);
        return areas.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActivo()))
                .filter(a -> propia == null || propia.equals(a.getId()))
                .sorted(Comparator.comparing(Area::getNombre))
                .map(a -> new AreaOpcion(a.getId(), a.getNombre()))
                .toList();
    }

    /** Área a la que se limita el reporte; null significa todas (solo administrador). */
    private static Long alcance(UsuarioAutenticado usuario, Long areaSolicitada) {
        if (RolNombre.ADMINISTRADOR.equals(usuario.rol())) {
            return areaSolicitada;
        }
        if (RolNombre.SUPERVISOR.equals(usuario.rol())) {
            if (usuario.areaId() == null) {
                throw new ForbiddenException("Tu cuenta no tiene un área asignada.");
            }
            // El supervisor siempre ve su área, aunque intente pedir otra.
            return usuario.areaId();
        }
        throw new ForbiddenException("No tienes permisos para generar reportes.");
    }

    static void validarRango(LocalDate inicio, LocalDate fin) {
        if (inicio.isAfter(fin)) {
            throw new FieldValidationException("desde", "La fecha inicial no puede ser posterior a la final.");
        }
        if (ChronoUnit.DAYS.between(inicio, fin) + 1 > MAX_DIAS) {
            throw new FieldValidationException("hasta", "El rango no puede superar un año.");
        }
    }

    static FilaReporte fila(Solicitud s, LocalDateTime resolucion, LocalDateTime ahora) {
        boolean atendida = estaAtendida(s);
        LocalDateTime fechaResolucion = atendida ? resolucion : null;
        BigDecimal dias = fechaResolucion == null ? null : dias(s.getFechaCreacion(), fechaResolucion);

        String cumplimiento;
        if (atendida) {
            cumplimiento = fechaResolucion == null ? ATENDIDA_SIN_FECHA
                    : fechaResolucion.isAfter(s.getFechaVencimiento()) ? FilaReporte.FUERA_DE_PLAZO : FilaReporte.A_TIEMPO;
        } else {
            cumplimiento = s.getFechaVencimiento().isBefore(ahora) ? FilaReporte.VENCIDA : FilaReporte.EN_PLAZO;
        }

        return new FilaReporte(s.getCodigo(), s.getFechaCreacion(), s.getCategoria().getNombre(),
                s.getArea().getNombre(), s.getPrioridad().getNombre(), s.getEstado().getNombre(),
                s.getTecnico() == null ? null : s.getTecnico().getNombres(),
                s.getFechaVencimiento(), fechaResolucion, dias, cumplimiento);
    }

    private static ResumenReporte resumen(List<Solicitud> lista, List<FilaReporte> filas) {
        long atendidas = lista.stream().filter(ReporteService::estaAtendida).count();
        long vencidas = contar(filas, FilaReporte.VENCIDA);
        long aTiempo = contar(filas, FilaReporte.A_TIEMPO);
        long fueraDePlazo = contar(filas, FilaReporte.FUERA_DE_PLAZO);

        BigDecimal cumplimiento = aTiempo + fueraDePlazo == 0 ? null
                : BigDecimal.valueOf(aTiempo * 100).divide(BigDecimal.valueOf(aTiempo + fueraDePlazo), 1, RoundingMode.HALF_UP);

        List<BigDecimal> tiempos = filas.stream().map(FilaReporte::diasAtencion).filter(Objects::nonNull).toList();
        BigDecimal mttr = tiempos.isEmpty() ? null : tiempos.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(tiempos.size()), 1, RoundingMode.HALF_UP);

        return new ResumenReporte(lista.size(), atendidas, lista.size() - atendidas, vencidas, fueraDePlazo,
                cumplimiento, mttr,
                agrupar(lista, s -> s.getEstado().getNombre(), Comparator.comparing(s -> s.getEstado().getOrden())),
                agrupar(lista, s -> s.getCategoria().getNombre(), Comparator.comparing(s -> s.getCategoria().getNombre())),
                agrupar(lista, s -> s.getPrioridad().getNombre(), Comparator.comparing(s -> s.getPrioridad().getSlaDias())));
    }

    /** Matriz de carga: por técnico, cuántas tiene asignadas, en proceso y resueltas en el periodo. */
    private static List<CargaTecnico> cargaTecnica(List<FilaReporte> filas) {
        Map<String, long[]> porTecnico = new TreeMap<>();
        for (FilaReporte f : filas) {
            if (f.tecnico() == null) {
                continue;
            }
            long[] c = porTecnico.computeIfAbsent(f.tecnico(), t -> new long[3]);
            switch (f.estado()) {
                case EstadoNombre.ASIGNADO -> c[0]++;
                case EstadoNombre.EN_PROCESO -> c[1]++;
                case EstadoNombre.RESUELTO, EstadoNombre.CERRADO -> c[2]++;
                default -> {
                    // Pendiente o En revisión no tienen técnico asignado.
                }
            }
        }
        return porTecnico.entrySet().stream()
                .map(e -> new CargaTecnico(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .toList();
    }

    private static List<ConteoItem> agrupar(
            List<Solicitud> lista, Function<Solicitud, String> clave, Comparator<Solicitud> orden) {
        Map<String, Long> conteo = new LinkedHashMap<>();
        lista.stream().sorted(orden).forEach(s -> conteo.merge(clave.apply(s), 1L, Long::sum));
        return conteo.entrySet().stream().map(e -> new ConteoItem(e.getKey(), e.getValue())).toList();
    }

    private static long contar(List<FilaReporte> filas, String cumplimiento) {
        return filas.stream().filter(f -> cumplimiento.equals(f.cumplimiento())).count();
    }

    private static boolean estaAtendida(Solicitud s) {
        String estado = s.getEstado().getNombre();
        return EstadoNombre.RESUELTO.equals(estado) || EstadoNombre.CERRADO.equals(estado);
    }

    static BigDecimal dias(LocalDateTime inicio, LocalDateTime fin) {
        long minutos = Math.max(0, Duration.between(inicio, fin).toMinutes());
        return BigDecimal.valueOf(minutos).divide(MINUTOS_POR_DIA, 1, RoundingMode.HALF_UP);
    }
}
