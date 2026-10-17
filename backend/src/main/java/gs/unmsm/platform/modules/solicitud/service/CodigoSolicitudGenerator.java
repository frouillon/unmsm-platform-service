package gs.unmsm.platform.modules.solicitud.service;

import gs.unmsm.platform.modules.solicitud.repository.CorrelativoSolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CodigoSolicitudGenerator {

    private final CorrelativoSolicitudRepository correlativos;

    /** Debe ejecutarse dentro de la transacción que guarda la solicitud: LAST_INSERT_ID() es por conexión. */
    @Transactional(propagation = Propagation.MANDATORY)
    public String siguiente(int anio) {
        correlativos.incrementar(anio);
        return "SOL-%d-%04d".formatted(anio, correlativos.ultimoGenerado());
    }
}
