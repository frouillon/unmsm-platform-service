package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.model.Prioridad;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/** Fecha límite de atención según el plazo (SLA) de la prioridad. No contempla feriados. */
public final class SlaCalculator {

    private SlaCalculator() {
    }

    public static LocalDateTime vencimiento(LocalDateTime inicio, Prioridad prioridad) {
        int dias = prioridad.getSlaDias();
        if (!Boolean.TRUE.equals(prioridad.getDiasHabiles())) {
            return inicio.plusDays(dias);
        }
        LocalDateTime fecha = inicio;
        int sumados = 0;
        while (sumados < dias) {
            fecha = fecha.plusDays(1);
            if (esHabil(fecha.getDayOfWeek())) {
                sumados++;
            }
        }
        return fecha;
    }

    private static boolean esHabil(DayOfWeek dia) {
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
    }
}
