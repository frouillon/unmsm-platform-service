package gs.unmsm.platform.modules.solicitud.model;

import jakarta.persistence.*;
import lombok.*;

/** Último número de solicitud emitido por año; respalda el código SOL-AAAA-XXXX. */
@Entity
@Table(name = "correlativo_solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorrelativoSolicitud {

    @Id
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "ultimo", nullable = false)
    private Integer ultimo;
}
