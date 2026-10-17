package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.model.Prioridad;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SlaCalculatorTest {

    private static Prioridad prioridad(int dias, boolean habiles) {
        return Prioridad.builder().nombre("P").slaDias(dias).diasHabiles(habiles).build();
    }

    @Test
    void diasHabilesSaltanFinesDeSemana() {
        LocalDateTime viernes = LocalDateTime.of(2026, 10, 16, 10, 30);
        assertThat(SlaCalculator.vencimiento(viernes, prioridad(7, true)))
                .isEqualTo(LocalDateTime.of(2026, 10, 27, 10, 30));
        assertThat(SlaCalculator.vencimiento(viernes, prioridad(1, true)))
                .isEqualTo(LocalDateTime.of(2026, 10, 19, 10, 30));
    }

    @Test
    void registroEnFinDeSemanaEmpiezaAContarElLunes() {
        LocalDateTime sabado = LocalDateTime.of(2026, 10, 17, 9, 0);
        assertThat(SlaCalculator.vencimiento(sabado, prioridad(3, true)))
                .isEqualTo(LocalDateTime.of(2026, 10, 21, 9, 0));
    }

    @Test
    void diasCalendarioNoSaltanFinesDeSemana() {
        LocalDateTime viernes = LocalDateTime.of(2026, 10, 16, 18, 0);
        assertThat(SlaCalculator.vencimiento(viernes, prioridad(1, false)))
                .isEqualTo(LocalDateTime.of(2026, 10, 17, 18, 0));
    }
}
