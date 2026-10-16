package gs.unmsm.platform.modules.solicitud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prioridad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "sla_dias", nullable = false)
    private Integer slaDias;

    @Column(name = "dias_habiles", nullable = false)
    @Builder.Default
    private Boolean diasHabiles = true;
}