package gs.unmsm.platform.modules.reporte.service;

import gs.unmsm.platform.modules.reporte.dto.FilaReporte;
import gs.unmsm.platform.modules.reporte.dto.ReporteSolicitudesResponse;
import gs.unmsm.platform.modules.reporte.dto.ResumenReporte;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReporteCsvTest {

    @Test
    void generaCsvConBomCabeceraYFilasEntreComillas() {
        FilaReporte fila = new FilaReporte("SOL-2026-0001", LocalDateTime.of(2026, 11, 10, 9, 30), "Mantenimiento",
                "Mantenimiento general", "Alta", "Resuelto", "Diana Cárdenas", LocalDateTime.of(2026, 11, 13, 9, 30),
                LocalDateTime.of(2026, 11, 11, 21, 30), new BigDecimal("1.5"), FilaReporte.A_TIEMPO);
        FilaReporte abierta = new FilaReporte("SOL-2026-0002", LocalDateTime.of(2026, 11, 12, 8, 0), "Mantenimiento",
                "Mantenimiento general", "Media", "Pendiente", null, LocalDateTime.of(2026, 11, 21, 8, 0),
                null, null, FilaReporte.EN_PLAZO);

        String csv = new String(ReporteCsv.generar(reporte(List.of(fila, abierta))), StandardCharsets.UTF_8);
        String[] lineas = csv.split("\r\n");

        assertThat(csv).startsWith("﻿");
        assertThat(lineas).hasSize(3);
        assertThat(lineas[0]).isEqualTo("﻿\"Código\",\"Fecha de registro\",\"Categoría\",\"Área\",\"Prioridad\","
                + "\"Estado\",\"Técnico\",\"Fecha límite\",\"Fecha de resolución\",\"Días de atención\",\"Cumplimiento de SLA\"");
        assertThat(lineas[1]).isEqualTo("\"SOL-2026-0001\",\"2026-11-10 09:30\",\"Mantenimiento\",\"Mantenimiento general\","
                + "\"Alta\",\"Resuelto\",\"Diana Cárdenas\",\"2026-11-13 09:30\",\"2026-11-11 21:30\",\"1.5\",\"A tiempo\"");
        assertThat(lineas[2]).contains("\"Pendiente\",\"\",\"2026-11-21 08:00\",\"\",\"\",\"En plazo\"");
    }

    @Test
    void neutralizaFormulasYEscapaComillas() {
        assertThat(ReporteCsv.celda("=HYPERLINK(\"http://malo\")")).isEqualTo("\"'=HYPERLINK(\"\"http://malo\"\")\"");
        assertThat(ReporteCsv.celda("+51 999")).isEqualTo("\"'+51 999\"");
        assertThat(ReporteCsv.celda("@SUM(A1)")).isEqualTo("\"'@SUM(A1)\"");
        assertThat(ReporteCsv.celda("Aula 4B, piso 2")).isEqualTo("\"Aula 4B, piso 2\"");
        assertThat(ReporteCsv.celda(null)).isEqualTo("\"\"");
    }

    @Test
    void elNombreDelArchivoIncluyeElRango() {
        assertThat(ReporteCsv.nombreArchivo(reporte(List.of())))
                .isEqualTo("reporte-solicitudes_2026-11-01_2026-11-30.csv");
    }

    private static ReporteSolicitudesResponse reporte(List<FilaReporte> filas) {
        ResumenReporte resumen = new ResumenReporte(filas.size(), 0, 0, 0, 0, null, null, List.of(), List.of(), List.of());
        return new ReporteSolicitudesResponse(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                "Mantenimiento general", LocalDateTime.of(2026, 11, 26, 12, 0), resumen, List.of(), filas);
    }
}
