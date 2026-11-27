package gs.unmsm.platform.modules.reporte.service;

import gs.unmsm.platform.modules.reporte.dto.FilaReporte;
import gs.unmsm.platform.modules.reporte.dto.ReporteSolicitudesResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Convierte el reporte a CSV (RFC 4180) en UTF-8 con BOM, para que Excel muestre bien las tildes.
 *
 * <p>Toda celda va entre comillas y las que empiezan con {@code = + - @} o tabulación se anteponen con un
 * apóstrofo: así un asunto o nombre malicioso no se ejecuta como fórmula al abrir el archivo (CSV injection).</p>
 */
public final class ReporteCsv {

    static final List<String> CABECERA = List.of(
            "Código", "Fecha de registro", "Categoría", "Área", "Prioridad", "Estado", "Técnico",
            "Fecha límite", "Fecha de resolución", "Días de atención", "Cumplimiento de SLA");

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String BOM = "﻿";
    private static final String FIN_DE_LINEA = "\r\n";

    private ReporteCsv() {
    }

    public static byte[] generar(ReporteSolicitudesResponse reporte) {
        StringBuilder csv = new StringBuilder(BOM);
        linea(csv, CABECERA);
        for (FilaReporte f : reporte.filas()) {
            linea(csv, List.of(
                    f.codigo(),
                    fecha(f.fechaCreacion()),
                    f.categoria(),
                    f.area(),
                    f.prioridad(),
                    f.estado(),
                    texto(f.tecnico()),
                    fecha(f.fechaVencimiento()),
                    fecha(f.fechaResolucion()),
                    f.diasAtencion() == null ? "" : f.diasAtencion().toPlainString(),
                    f.cumplimiento()));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static String nombreArchivo(ReporteSolicitudesResponse reporte) {
        return "reporte-solicitudes_%s_%s.csv".formatted(reporte.desde(), reporte.hasta());
    }

    private static void linea(StringBuilder csv, List<String> celdas) {
        for (int i = 0; i < celdas.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(celda(celdas.get(i)));
        }
        csv.append(FIN_DE_LINEA);
    }

    static String celda(String valor) {
        String v = valor == null ? "" : valor;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return '"' + v.replace("\"", "\"\"") + '"';
    }

    private static String fecha(LocalDateTime fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
