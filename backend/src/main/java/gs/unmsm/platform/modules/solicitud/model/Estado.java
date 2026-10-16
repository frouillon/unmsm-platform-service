package gs.unmsm.platform.modules.solicitud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden;
}