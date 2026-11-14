package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.model.Prioridad;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF-27 — Motor de SLA: fecha de vencimiento según la prioridad.
 * <p>
 * Técnica: análisis de valores límite alrededor del fin de semana, que es donde
 * cambia el resultado entre días hábiles y días calendario.
 * Octubre de 2026: viernes 16, sábado 17, domingo 18, lunes 19, martes 20, miércoles 21.
 */
class SlaCalculatorLimitesTest {

    private static final LocalDateTime VIERNES = LocalDateTime.of(2026, 10, 16, 10, 30);
    private static final LocalDateTime SABADO = LocalDateTime.of(2026, 10, 17, 10, 30);
    private static final LocalDateTime DOMINGO = LocalDateTime.of(2026, 10, 18, 10, 30);
    private static final LocalDateTime LUNES = LocalDateTime.of(2026, 10, 19, 10, 30);
    private static final LocalDateTime MIERCOLES = LocalDateTime.of(2026, 10, 21, 10, 30);

    @Test
    void tresDiasHabilesDesdeElViernesVencenElMiercoles() {
        assertThat(SlaCalculator.vencimiento(VIERNES, diasHabiles(3))).isEqualTo(MIERCOLES);
    }

    @Test
    void unDiaHabilDesdeElDomingoVenceElLunes() {
        assertThat(SlaCalculator.vencimiento(DOMINGO, diasHabiles(1))).isEqualTo(LUNES);
    }

    @Test
    void losDiasCalendarioSiCuentanElFinDeSemana() {
        assertThat(SlaCalculator.vencimiento(VIERNES, diasCalendario(2))).isEqualTo(DOMINGO);
        assertThat(SlaCalculator.vencimiento(SABADO, diasCalendario(1))).isEqualTo(DOMINGO);
    }

    private static Prioridad diasHabiles(int dias) {
        return Prioridad.builder().nombre("Alta").slaDias(dias).diasHabiles(true).build();
    }

    private static Prioridad diasCalendario(int dias) {
        return Prioridad.builder().nombre("Urgente").slaDias(dias).diasHabiles(false).build();
    }
}
